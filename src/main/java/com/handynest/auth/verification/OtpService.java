package com.handynest.auth.verification;

import com.handynest.auth.api.PhoneOtpRequestResponse;
import com.handynest.auth.api.PhoneOtpVerifyResponse;
import com.handynest.common.error.BadRequestBusinessException;
import com.handynest.common.error.RateLimitExceededException;
import com.handynest.identity.User;
import com.handynest.identity.UserRepository;
import com.handynest.performer.PerformerProfileRepository;
import com.handynest.platform.PlatformSettingKey;
import com.handynest.platform.PlatformSettingService;
import jakarta.servlet.http.HttpServletRequest;
import java.security.SecureRandom;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OtpService {

  private static final int OTP_BOUND = 1_000_000;
  private static final int IP_MAX_LENGTH = 64;

  private final OtpCodeRepository otpCodeRepository;
  private final UserRepository userRepository;
  private final PerformerProfileRepository performerProfileRepository;
  private final OtpProperties otpProperties;
  private final SmsProvider smsProvider;
  private final VerificationHashService hashService;
  private final PlatformSettingService platformSettingService;
  private final SecureRandom secureRandom = new SecureRandom();

  @Transactional
  public PhoneOtpRequestResponse requestPhoneOtp(User user, HttpServletRequest servletRequest) {
    if (!otpProperties.isEnabled()
        || !platformSettingService.booleanValue(PlatformSettingKey.PHONE_VERIFICATION_ENABLED)) {
      throw new BadRequestBusinessException("Phone OTP is disabled");
    }
    String phone = requirePhone(user);
    Instant now = Instant.now();

    OtpCode latest =
        otpCodeRepository
            .findFirstByUserIdAndPhoneNumberAndPurposeAndConsumedAtIsNullOrderByCreatedAtDesc(
                user.getId(), phone, OtpPurpose.PHONE_VERIFICATION)
            .orElse(null);
    if (latest != null && latest.getResendAvailableAt().isAfter(now)) {
      throw new RateLimitExceededException(secondsUntil(latest.getResendAvailableAt(), now));
    }

    String rawCode = newOtpCode();
    OtpCode otpCode =
        otpCodeRepository.save(
            new OtpCode(
                user,
                phone,
                OtpPurpose.PHONE_VERIFICATION,
                hashService.sha256(rawCode),
                otpProperties.getMaxAttempts(),
                now.plus(otpProperties.getTtl()),
                now.plus(otpProperties.getResendCooldown()),
                now,
                truncate(clientIp(servletRequest), IP_MAX_LENGTH)));
    smsProvider.sendOtp(phone, rawCode);

    return new PhoneOtpRequestResponse(
        otpCode.getExpiresAt(), otpProperties.getResendCooldown().toSeconds(), maskPhone(phone));
  }

  @Transactional(noRollbackFor = BadRequestBusinessException.class)
  public PhoneOtpVerifyResponse verifyPhoneOtp(User user, String rawCode) {
    if (!otpProperties.isEnabled()
        || !platformSettingService.booleanValue(PlatformSettingKey.PHONE_VERIFICATION_ENABLED)) {
      throw new BadRequestBusinessException("Phone OTP is disabled");
    }
    String phone = requirePhone(user);
    Instant now = Instant.now();

    OtpCode otpCode =
        otpCodeRepository
            .findFirstByUserIdAndPhoneNumberAndPurposeAndConsumedAtIsNullOrderByCreatedAtDesc(
                user.getId(), phone, OtpPurpose.PHONE_VERIFICATION)
            .orElseThrow(() -> new BadRequestBusinessException("Invalid or expired OTP code"));

    if (!otpCode.isActive(now)) {
      throw new BadRequestBusinessException("Invalid or expired OTP code");
    }
    if (!otpCode.getCodeHash().equals(hashService.sha256(rawCode))) {
      otpCode.incrementAttempts();
      throw new BadRequestBusinessException("Invalid or expired OTP code");
    }

    otpCodeRepository.consumeActiveCodes(user.getId(), phone, OtpPurpose.PHONE_VERIFICATION, now);
    user.setPhoneVerified(true);
    userRepository.save(user);
    performerProfileRepository
        .findByUserId(user.getId())
        .ifPresent(profile -> profile.markPhoneVerified(now));
    return new PhoneOtpVerifyResponse(true);
  }

  private String requirePhone(User user) {
    if (user.getPhone() == null || user.getPhone().isBlank()) {
      throw new BadRequestBusinessException("Phone number is required");
    }
    return user.getPhone().trim();
  }

  private String newOtpCode() {
    return "%06d".formatted(secureRandom.nextInt(OTP_BOUND));
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

  private String maskPhone(String phone) {
    if (phone.length() <= 4) {
      return "****";
    }
    return "****" + phone.substring(phone.length() - 4);
  }
}
