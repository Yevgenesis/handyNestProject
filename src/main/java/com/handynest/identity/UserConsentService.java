package com.handynest.identity;

import com.handynest.common.error.BadRequestBusinessException;
import com.handynest.common.error.ConsentRequiredException;
import com.handynest.platform.PlatformSettingKey;
import com.handynest.platform.PlatformSettingService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserConsentService {

  private static final Set<ConsentType> REGISTRATION_REQUIRED =
      EnumSet.of(
          ConsentType.TERMS_OF_SERVICE,
          ConsentType.PRIVACY_POLICY,
          ConsentType.PERSONAL_DATA_PROCESSING);
  private static final int IP_MAX_LENGTH = 64;
  private static final int USER_AGENT_MAX_LENGTH = 500;

  private final UserConsentRepository repository;
  private final UserProfileService userProfileService;
  private final PlatformSettingService platformSettingService;

  @Transactional(readOnly = true)
  public List<ConsentRequirementResponse> requirements() {
    return List.of(ConsentType.values()).stream()
        .map(
            type ->
                new ConsentRequirementResponse(
                    type, currentVersion(type), REGISTRATION_REQUIRED.contains(type)))
        .toList();
  }

  @Transactional(readOnly = true)
  public List<UserConsentResponse> myConsents(UserDetails userDetails) {
    User user = userProfileService.currentUser(userDetails);
    return repository.findAllByUserIdOrderByAcceptedAtDesc(user.getId()).stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional
  public List<UserConsentResponse> accept(
      UserDetails userDetails,
      List<ConsentAcceptanceRequest> requests,
      HttpServletRequest servletRequest) {
    User user = userProfileService.currentUser(userDetails);
    accept(user, requests, servletRequest, Set.of());
    return myConsents(userDetails);
  }

  @Transactional
  public void acceptRegistrationConsents(
      User user, List<ConsentAcceptanceRequest> requests, HttpServletRequest servletRequest) {
    accept(user, requests, servletRequest, REGISTRATION_REQUIRED);
  }

  @Transactional(readOnly = true)
  public void requireCurrent(User user, ConsentType type) {
    String version = currentVersion(type);
    if (!repository.existsByUserIdAndConsentTypeAndDocumentVersion(user.getId(), type, version)) {
      throw new ConsentRequiredException(
          "Consent " + type.name() + " version " + version + " is required");
    }
  }

  public String currentVersion(ConsentType type) {
    return platformSettingService.stringValue(settingKey(type));
  }

  private void accept(
      User user,
      List<ConsentAcceptanceRequest> requests,
      HttpServletRequest servletRequest,
      Set<ConsentType> required) {
    if (requests == null || requests.isEmpty()) {
      throw new BadRequestBusinessException("At least one consent is required");
    }

    Map<ConsentType, ConsentAcceptanceRequest> unique = new EnumMap<>(ConsentType.class);
    for (ConsentAcceptanceRequest request : requests) {
      if (request == null || request.type() == null) {
        throw new BadRequestBusinessException("Consent type is required");
      }
      if (unique.put(request.type(), request) != null) {
        throw new BadRequestBusinessException("Duplicate consent type: " + request.type());
      }
      String currentVersion = currentVersion(request.type());
      if (!currentVersion.equals(request.documentVersion())) {
        throw new BadRequestBusinessException(
            "Outdated consent version for " + request.type().name());
      }
    }
    if (!unique.keySet().containsAll(required)) {
      throw new BadRequestBusinessException("Required registration consents are missing");
    }

    Instant acceptedAt = Instant.now();
    String ipAddress = truncate(clientIp(servletRequest), IP_MAX_LENGTH);
    String userAgent = truncate(servletRequest.getHeader("User-Agent"), USER_AGENT_MAX_LENGTH);
    for (ConsentAcceptanceRequest request : unique.values()) {
      if (repository.existsByUserIdAndConsentTypeAndDocumentVersion(
          user.getId(), request.type(), request.documentVersion())) {
        continue;
      }
      try {
        repository.save(
            new UserConsent(
                user, request.type(), request.documentVersion(), acceptedAt, ipAddress, userAgent));
      } catch (DataIntegrityViolationException ignored) {
        // Concurrent acceptance of the same immutable consent is idempotent.
      }
    }
  }

  private PlatformSettingKey settingKey(ConsentType type) {
    return switch (type) {
      case TERMS_OF_SERVICE -> PlatformSettingKey.TERMS_VERSION;
      case PRIVACY_POLICY -> PlatformSettingKey.PRIVACY_POLICY_VERSION;
      case PERSONAL_DATA_PROCESSING -> PlatformSettingKey.PERSONAL_DATA_VERSION;
      case PERFORMER_RULES -> PlatformSettingKey.PERFORMER_RULES_VERSION;
      case CUSTOMER_RULES -> PlatformSettingKey.CUSTOMER_RULES_VERSION;
      case PROHIBITED_SERVICES_POLICY -> PlatformSettingKey.PROHIBITED_SERVICES_POLICY_VERSION;
      case PAYMENT_POLICY -> PlatformSettingKey.PAYMENT_POLICY_VERSION;
    };
  }

  private UserConsentResponse toResponse(UserConsent consent) {
    return new UserConsentResponse(
        consent.getConsentType(), consent.getDocumentVersion(), consent.getAcceptedAt());
  }

  private String clientIp(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isBlank()) {
      return forwarded.split(",", 2)[0].trim();
    }
    return request.getRemoteAddr() == null ? "unknown" : request.getRemoteAddr();
  }

  private String truncate(String value, int maxLength) {
    if (value == null) {
      return null;
    }
    return value.length() <= maxLength ? value : value.substring(0, maxLength);
  }
}
