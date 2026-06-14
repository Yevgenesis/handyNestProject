package com.handynest.auth.verification;

import com.handynest.auth.api.EmailVerificationRequestResponse;
import com.handynest.auth.api.EmailVerificationVerifyResponse;
import com.handynest.common.error.BadRequestBusinessException;
import com.handynest.common.error.RateLimitExceededException;
import com.handynest.identity.User;
import com.handynest.identity.UserRepository;
import com.handynest.platform.PlatformSettingKey;
import com.handynest.platform.PlatformSettingService;
import jakarta.servlet.http.HttpServletRequest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

  private static final int TOKEN_BYTE_LENGTH = 48;
  private static final int IP_MAX_LENGTH = 64;

  private final EmailVerificationTokenRepository tokenRepository;
  private final UserRepository userRepository;
  private final EmailVerificationProperties properties;
  private final EmailProvider emailProvider;
  private final VerificationHashService hashService;
  private final PlatformSettingService platformSettingService;
  private final SecureRandom secureRandom = new SecureRandom();

  @Transactional
  public EmailVerificationRequestResponse requestVerification(
      User user, HttpServletRequest servletRequest) {
    if (!properties.isEnabled()
        || !platformSettingService.booleanValue(PlatformSettingKey.EMAIL_VERIFICATION_ENABLED)) {
      throw new BadRequestBusinessException("Email verification is disabled");
    }
    String email = normalizeEmail(user.getEmail());
    Instant now = Instant.now();

    EmailVerificationToken latest =
        tokenRepository
            .findFirstByUserIdAndEmailAndConsumedAtIsNullOrderByCreatedAtDesc(user.getId(), email)
            .orElse(null);
    if (latest != null && latest.getResendAvailableAt().isAfter(now)) {
      throw new RateLimitExceededException(secondsUntil(latest.getResendAvailableAt(), now));
    }

    String rawToken = newRawToken();
    EmailVerificationToken token =
        tokenRepository.save(
            new EmailVerificationToken(
                user,
                email,
                hashService.sha256(rawToken),
                now.plus(properties.getTtl()),
                now.plus(properties.getResendCooldown()),
                now,
                truncate(clientIp(servletRequest), IP_MAX_LENGTH)));
    emailProvider.sendVerificationToken(email, rawToken);

    return new EmailVerificationRequestResponse(
        token.getExpiresAt(), properties.getResendCooldown().toSeconds(), maskEmail(email));
  }

  @Transactional
  public EmailVerificationVerifyResponse verify(String rawToken) {
    if (!properties.isEnabled()
        || !platformSettingService.booleanValue(PlatformSettingKey.EMAIL_VERIFICATION_ENABLED)) {
      throw new BadRequestBusinessException("Email verification is disabled");
    }

    Instant now = Instant.now();
    EmailVerificationToken token =
        tokenRepository
            .findByTokenHash(hashService.sha256(rawToken))
            .orElseThrow(
                () ->
                    new BadRequestBusinessException("Invalid or expired email verification token"));
    if (!token.isActive(now)) {
      throw new BadRequestBusinessException("Invalid or expired email verification token");
    }

    User user = token.getUser();
    if (!normalizeEmail(user.getEmail()).equals(token.getEmail())) {
      throw new BadRequestBusinessException("Invalid or expired email verification token");
    }

    tokenRepository.consumeActiveTokens(user.getId(), token.getEmail(), now);
    user.setEmailVerified(true);
    userRepository.save(user);
    return new EmailVerificationVerifyResponse(true);
  }

  private String newRawToken() {
    byte[] tokenBytes = new byte[TOKEN_BYTE_LENGTH];
    secureRandom.nextBytes(tokenBytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
  }

  private long secondsUntil(Instant target, Instant now) {
    return Math.max(1, target.getEpochSecond() - now.getEpochSecond());
  }

  private String clientIp(HttpServletRequest servletRequest) {
    String forwardedFor = servletRequest.getHeader("X-Forwarded-For");
    if (forwardedFor != null && !forwardedFor.isBlank()) {
      return forwardedFor.split(",")[0].trim();
    }
    return servletRequest.getRemoteAddr();
  }

  private String truncate(String value, int maxLength) {
    if (value == null || value.length() <= maxLength) {
      return value;
    }
    return value.substring(0, maxLength);
  }

  private String normalizeEmail(String email) {
    return email.trim().toLowerCase(Locale.ROOT);
  }

  private String maskEmail(String email) {
    String[] parts = email.split("@", 2);
    String local = parts[0].isEmpty() ? "*" : parts[0].substring(0, 1) + "***";
    return local + "@" + parts[1];
  }
}
