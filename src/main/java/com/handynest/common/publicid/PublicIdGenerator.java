package com.handynest.common.publicid;

import java.security.SecureRandom;
import java.time.Clock;
import java.util.Objects;
import java.util.Random;
import java.util.regex.Pattern;

public final class PublicIdGenerator {

  private static final char[] CROCKFORD_BASE32 = "0123456789ABCDEFGHJKMNPQRSTVWXYZ".toCharArray();
  private static final Pattern ULID_PATTERN = Pattern.compile("^[0-9A-HJKMNP-TV-Z]{26}$");
  private static final PublicIdGenerator DEFAULT =
      new PublicIdGenerator(Clock.systemUTC(), new SecureRandom());

  private final Clock clock;
  private final Random random;

  public PublicIdGenerator(Clock clock, Random random) {
    this.clock = Objects.requireNonNull(clock, "clock must not be null");
    this.random = Objects.requireNonNull(random, "random must not be null");
  }

  public static PublicIdGenerator defaultGenerator() {
    return DEFAULT;
  }

  public String newUlid() {
    byte[] bytes = new byte[16];
    long timestamp = clock.millis();

    bytes[0] = (byte) (timestamp >>> 40);
    bytes[1] = (byte) (timestamp >>> 32);
    bytes[2] = (byte) (timestamp >>> 24);
    bytes[3] = (byte) (timestamp >>> 16);
    bytes[4] = (byte) (timestamp >>> 8);
    bytes[5] = (byte) timestamp;

    byte[] randomBytes = new byte[10];
    random.nextBytes(randomBytes);
    System.arraycopy(randomBytes, 0, bytes, 6, randomBytes.length);

    return encode(bytes);
  }

  public boolean isValid(String publicId) {
    return publicId != null && ULID_PATTERN.matcher(publicId).matches();
  }

  private static String encode(byte[] bytes) {
    char[] encoded = new char[26];
    int index = 0;
    int buffer = 0;
    int bitsLeft = 0;

    for (byte currentByte : bytes) {
      buffer = (buffer << 8) | (currentByte & 0xff);
      bitsLeft += 8;

      while (bitsLeft >= 5) {
        encoded[index++] = CROCKFORD_BASE32[(buffer >>> (bitsLeft - 5)) & 0x1f];
        bitsLeft -= 5;
      }
    }

    if (bitsLeft > 0) {
      encoded[index++] = CROCKFORD_BASE32[(buffer << (5 - bitsLeft)) & 0x1f];
    }

    while (index < encoded.length) {
      encoded[index++] = '0';
    }

    return new String(encoded);
  }
}
