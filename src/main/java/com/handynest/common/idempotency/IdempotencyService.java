package com.handynest.common.idempotency;

import com.handynest.common.error.BadRequestBusinessException;
import com.handynest.common.error.ConflictBusinessException;
import com.handynest.common.error.IdempotencyKeyConflictException;
import com.handynest.identity.User;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

  private static final int MAX_KEY_LENGTH = 120;

  private final IdempotencyKeyRepository idempotencyKeyRepository;
  private final IdempotencyProperties idempotencyProperties;

  @Transactional
  public IdempotencyDecision begin(
      User user, String key, String targetEndpoint, String requestBody) {
    String normalizedKey = requireKey(key);
    String requestHash = sha256(requestBody == null ? "" : requestBody);
    IdempotencyKey existing =
        user == null
            ? idempotencyKeyRepository
                .findByUserIsNullAndKeyAndTargetEndpoint(normalizedKey, targetEndpoint)
                .orElse(null)
            : idempotencyKeyRepository
                .findByUserIdAndKeyAndTargetEndpoint(user.getId(), normalizedKey, targetEndpoint)
                .orElse(null);

    if (existing != null) {
      if (!existing.getRequestHash().equals(requestHash)) {
        throw new IdempotencyKeyConflictException();
      }
      if (existing.getResponseStatus() == null || existing.getResponseBodyHash() == null) {
        throw new ConflictBusinessException("Idempotent request is still being processed");
      }
      return new IdempotencyDecision(existing, true);
    }

    IdempotencyKey created =
        idempotencyKeyRepository.save(
            new IdempotencyKey(
                user,
                normalizedKey,
                requestHash,
                targetEndpoint,
                Instant.now().plus(idempotencyProperties.getRequestTtl())));
    return new IdempotencyDecision(created, false);
  }

  public void complete(IdempotencyKey key, int responseStatus, String responseBody) {
    complete(key, responseStatus, responseBody, null, null);
  }

  public void complete(
      IdempotencyKey key,
      int responseStatus,
      String responseBody,
      String responseResourceType,
      String responseResourceId) {
    key.complete(
        responseStatus,
        sha256(responseBody == null ? "" : responseBody),
        responseResourceType,
        responseResourceId);
  }

  public String sha256(String value) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 is not available", exception);
    }
  }

  @Scheduled(fixedDelayString = "#{@idempotencyProperties.cleanupInterval.toMillis()}")
  @Transactional
  public void cleanupExpiredKeys() {
    cleanupExpiredKeys(Instant.now());
  }

  @Transactional
  public long cleanupExpiredKeys(Instant now) {
    return idempotencyKeyRepository.deleteByExpiresAtBefore(now);
  }

  private String requireKey(String key) {
    if (key == null || key.isBlank()) {
      throw new BadRequestBusinessException("Idempotency-Key header is required");
    }
    String normalizedKey = key.trim();
    if (normalizedKey.length() > MAX_KEY_LENGTH) {
      throw new BadRequestBusinessException("Idempotency-Key header is too long");
    }
    return normalizedKey;
  }
}
