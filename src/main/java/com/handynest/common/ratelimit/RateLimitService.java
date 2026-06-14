package com.handynest.common.ratelimit;

import com.handynest.common.error.RateLimitExceededException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RateLimitService {

  private static final long NANOS_PER_SECOND = 1_000_000_000L;

  private final RateLimitProperties properties;
  private final ConcurrentMap<String, BucketEntry> buckets = new ConcurrentHashMap<>();
  private final Clock clock = Clock.systemUTC();

  public void consumeAuthRegister(HttpServletRequest servletRequest) {
    consume("auth:register:" + clientIp(servletRequest), properties.getRegister());
  }

  public void consumeAuthLogin(String email, HttpServletRequest servletRequest) {
    consume(
        "auth:login:" + clientIp(servletRequest) + ":" + normalizeKeyPart(email),
        properties.getLogin());
  }

  public void consumeAuthRefresh(HttpServletRequest servletRequest) {
    consume("auth:refresh:" + clientIp(servletRequest), properties.getRefresh());
  }

  public void consumePhoneOtpRequest(String phone, HttpServletRequest servletRequest) {
    consume(
        "auth:otp-request:" + clientIp(servletRequest) + ":" + normalizeKeyPart(phone),
        properties.getOtpRequest());
  }

  public void consumePhoneOtpVerify(String phone, HttpServletRequest servletRequest) {
    consume(
        "auth:otp-verify:" + clientIp(servletRequest) + ":" + normalizeKeyPart(phone),
        properties.getOtpVerify());
  }

  public void consumeEmailVerificationRequest(String email, HttpServletRequest servletRequest) {
    consume(
        "auth:email-verify-request:" + clientIp(servletRequest) + ":" + normalizeKeyPart(email),
        properties.getEmailVerificationRequest());
  }

  @Scheduled(fixedDelayString = "#{@rateLimitProperties.cleanupInterval.toMillis()}")
  public void cleanupIdleBuckets() {
    if (!properties.isEnabled()) {
      buckets.clear();
      return;
    }

    Instant oldestAllowedAccess = Instant.now(clock).minus(properties.getIdleEntryTtl());
    buckets
        .entrySet()
        .removeIf(entry -> entry.getValue().getLastSeenAt().isBefore(oldestAllowedAccess));
  }

  private void consume(String key, RateLimitProperties.Limit limit) {
    if (!properties.isEnabled()) {
      return;
    }

    BucketEntry bucketEntry =
        buckets.computeIfAbsent(key, ignored -> new BucketEntry(newBucket(limit)));
    bucketEntry.markSeen(Instant.now(clock));

    ConsumptionProbe probe = bucketEntry.getBucket().tryConsumeAndReturnRemaining(1);
    if (!probe.isConsumed()) {
      throw new RateLimitExceededException(retryAfterSeconds(probe.getNanosToWaitForRefill()));
    }
  }

  private Bucket newBucket(RateLimitProperties.Limit limit) {
    return Bucket.builder()
        .addLimit(
            Bandwidth.builder()
                .capacity(limit.getCapacity())
                .refillIntervally(limit.getCapacity(), limit.getWindow())
                .build())
        .build();
  }

  private long retryAfterSeconds(long nanosToWaitForRefill) {
    if (nanosToWaitForRefill <= 0) {
      return 1;
    }

    return Math.max(1, (nanosToWaitForRefill + NANOS_PER_SECOND - 1) / NANOS_PER_SECOND);
  }

  private String clientIp(HttpServletRequest servletRequest) {
    String forwardedFor = servletRequest.getHeader("X-Forwarded-For");
    if (forwardedFor != null && !forwardedFor.isBlank()) {
      return forwardedFor.split(",")[0].trim();
    }
    return servletRequest.getRemoteAddr();
  }

  private String normalizeKeyPart(String value) {
    return value.trim().toLowerCase(Locale.ROOT);
  }

  private static class BucketEntry {

    private final Bucket bucket;
    private volatile Instant lastSeenAt;

    BucketEntry(Bucket bucket) {
      this.bucket = bucket;
      this.lastSeenAt = Instant.now();
    }

    Bucket getBucket() {
      return bucket;
    }

    Instant getLastSeenAt() {
      return lastSeenAt;
    }

    void markSeen(Instant instant) {
      this.lastSeenAt = instant;
    }
  }
}
