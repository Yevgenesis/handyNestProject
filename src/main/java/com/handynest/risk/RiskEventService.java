package com.handynest.risk;

import com.handynest.common.error.AccessDeniedBusinessException;
import com.handynest.common.error.BadRequestBusinessException;
import com.handynest.common.error.ResourceNotFoundException;
import com.handynest.identity.RoleName;
import com.handynest.identity.User;
import com.handynest.identity.UserProfileService;
import com.handynest.marketplace.ChatMessage;
import com.handynest.marketplace.ContactReveal;
import com.handynest.marketplace.Deal;
import com.handynest.marketplace.DealCancelReason;
import com.handynest.marketplace.MarketplaceTask;
import com.handynest.marketplace.MarketplaceTaskRepository;
import com.handynest.marketplace.TaskStatus;
import com.handynest.moderation.ModerationCase;
import com.handynest.moderation.ModerationCaseRepository;
import com.handynest.moderation.ModerationCaseStatus;
import com.handynest.moderation.ModerationPriority;
import com.handynest.moderation.ModerationTargetType;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RiskEventService {

  private final RiskDetectionService riskDetectionService;
  private final RiskEventRepository riskEventRepository;
  private final ModerationCaseRepository moderationCaseRepository;
  private final UserProfileService userProfileService;
  private final CustomerRiskService customerRiskService;
  private final CustomerRiskProperties customerRiskProperties;
  private final MarketplaceTaskRepository taskRepository;

  public boolean hasRisk(String text) {
    return riskDetectionService.hasRisk(text);
  }

  public void assertPreDealContentAllowed(String text) {
    riskDetectionService
        .detect(text)
        .ifPresent(
            detection -> {
              throw new BadRequestBusinessException(
                  "Contacts, external links and off-platform payment details are not allowed before a deal");
            });
  }

  public boolean chatMessageRiskFlag(String text, boolean phoneRevealed) {
    RiskDetectionResult detection = riskDetectionService.detect(text).orElse(null);
    if (detection == null) {
      return false;
    }
    if (detection.riskType() == RiskType.PAYMENT_OUTSIDE_PLATFORM) {
      return true;
    }
    if (detection.riskType() == RiskType.PHONE_SHARED && phoneRevealed) {
      return false;
    }
    throw new BadRequestBusinessException(
        "This contact channel is not available in the deal chat yet");
  }

  @Transactional
  public void recordDealCancellationAfterReveal(
      Deal deal,
      User actor,
      DealCancelReason reason,
      ContactReveal latestReveal,
      Instant canceledAt) {
    if (latestReveal == null
        || Duration.between(latestReveal.getCreatedAt(), canceledAt).compareTo(Duration.ofHours(2))
            > 0) {
      return;
    }
    saveDealRisk(
        actor,
        deal,
        RiskType.CONTACT_REVEAL_CANCELLATION,
        "Deal canceled within two hours after contact reveal",
        "contact-reveal-cancellation");
    if (reason == DealCancelReason.OTHER) {
      saveDealRisk(
          actor,
          deal,
          RiskType.CONTACT_REVEAL_OTHER_REASON,
          "OTHER cancellation reason used after contact reveal",
          "contact-reveal-other-reason");
    }
    createDealModerationCaseAfterThreshold(actor, deal);
  }

  @Transactional
  public void recordChatMessageRisk(ChatMessage message) {
    if (riskEventRepository.existsByChatMessageId(message.getId())) {
      return;
    }
    riskDetectionService
        .detect(message.getText())
        .ifPresent(
            detection -> {
              RiskEvent event = riskEventRepository.save(new RiskEvent(message, detection));
              customerRiskService.recordRiskEvent(event);
              if (detection.highRisk()) {
                createModerationCaseIfMissing(message, detection);
              }
            });
  }

  @Transactional
  public void recordCancellationPatternIfNeeded(MarketplaceTask task) {
    long cancellationCount =
        taskRepository.countByCustomerIdAndStatusAndCanceledAtAfter(
            task.getCustomer().getId(),
            TaskStatus.CANCELED,
            Instant.now().minus(customerRiskProperties.getCancellationWindow()));
    if (cancellationCount < customerRiskProperties.getCancellationEventThreshold()
        || riskEventRepository.existsByTaskIdAndRiskType(
            task.getId(), RiskType.MULTIPLE_CANCELLATIONS)) {
      return;
    }
    RiskSeverity severity =
        cancellationCount >= customerRiskProperties.getHighCancellationEventThreshold()
            ? RiskSeverity.HIGH
            : RiskSeverity.MEDIUM;
    String detectedText =
        cancellationCount
            + " task cancellations within "
            + customerRiskProperties.getCancellationWindow().toDays()
            + " days";
    RiskEvent event =
        riskEventRepository.save(
            new RiskEvent(
                task.getCustomer(),
                task,
                RiskType.MULTIPLE_CANCELLATIONS,
                severity,
                detectedText,
                Long.toString(cancellationCount)));
    customerRiskService.recordRiskEvent(event);
  }

  @Transactional(readOnly = true)
  public List<RiskEventResponse> adminEvents(
      UserDetails userDetails, RiskEventStatus status, RiskType riskType, RiskSeverity severity) {
    assertSupport(userDetails);
    RiskEventStatus effectiveStatus = status == null ? RiskEventStatus.OPEN : status;
    List<RiskEvent> events;
    if (riskType != null) {
      events =
          riskEventRepository.findAllByRiskTypeAndStatusOrderByCreatedAtDesc(
              riskType, effectiveStatus);
    } else if (severity != null) {
      events =
          riskEventRepository.findAllBySeverityAndStatusOrderByCreatedAtDesc(
              severity, effectiveStatus);
    } else {
      events = riskEventRepository.findAllByStatusOrderByCreatedAtDesc(effectiveStatus);
    }
    return events.stream().map(this::toResponse).toList();
  }

  @Transactional
  public RiskEventResponse resolve(
      UserDetails userDetails, String eventId, RiskEventResolutionRequest request) {
    User admin = assertAdmin(userDetails);
    RiskEvent event = findEvent(eventId);
    event.resolve(admin, comment(request), Instant.now());
    return toResponse(event);
  }

  @Transactional
  public RiskEventResponse markFalsePositive(
      UserDetails userDetails, String eventId, RiskEventResolutionRequest request) {
    User admin = assertAdmin(userDetails);
    RiskEvent event = findEvent(eventId);
    event.markFalsePositive(admin, comment(request), Instant.now());
    customerRiskService.reverseRiskEvent(event);
    return toResponse(event);
  }

  private void createModerationCaseIfMissing(ChatMessage message, RiskDetectionResult detection) {
    if (moderationCaseRepository.existsByTargetTypeAndTargetIdAndStatusIn(
        ModerationTargetType.CHAT_MESSAGE,
        message.getPublicId(),
        List.of(ModerationCaseStatus.OPEN, ModerationCaseStatus.IN_REVIEW))) {
      return;
    }
    moderationCaseRepository.save(
        new ModerationCase(
            ModerationTargetType.CHAT_MESSAGE,
            message.getPublicId(),
            message.getSender(),
            "High-risk chat message: " + detection.riskType().name(),
            priorityFor(detection.severity())));
  }

  private void saveDealRisk(
      User actor, Deal deal, RiskType type, String detectedText, String normalizedValue) {
    riskEventRepository.save(
        new RiskEvent(actor, deal, type, RiskSeverity.MEDIUM, detectedText, normalizedValue));
  }

  private void createDealModerationCaseAfterThreshold(User actor, Deal deal) {
    long recentSignals =
        riskEventRepository.countByUserIdAndRiskTypeInAndCreatedAtAfter(
            actor.getId(),
            List.of(RiskType.CONTACT_REVEAL_CANCELLATION),
            Instant.now().minus(Duration.ofDays(7)));
    if (recentSignals < 3
        || moderationCaseRepository.existsByTargetTypeAndTargetIdAndStatusIn(
            ModerationTargetType.DEAL,
            deal.getPublicId(),
            List.of(ModerationCaseStatus.OPEN, ModerationCaseStatus.IN_REVIEW))) {
      return;
    }
    moderationCaseRepository.save(
        new ModerationCase(
            ModerationTargetType.DEAL,
            deal.getPublicId(),
            actor,
            "Repeated contact reveal and cancellation pattern",
            ModerationPriority.HIGH));
  }

  private RiskEvent findEvent(String eventId) {
    return riskEventRepository
        .findByPublicId(eventId)
        .orElseThrow(() -> new ResourceNotFoundException("RiskEvent", eventId));
  }

  private User assertAdmin(UserDetails userDetails) {
    User user = userProfileService.currentUser(userDetails);
    if (!user.getRoles().contains(RoleName.ADMIN)) {
      throw new AccessDeniedBusinessException("Only admins can access risk events");
    }
    return user;
  }

  private User assertSupport(UserDetails userDetails) {
    User user = userProfileService.currentUser(userDetails);
    if (!user.getRoles().contains(RoleName.ADMIN)
        && !user.getRoles().contains(RoleName.MODERATOR)) {
      throw new AccessDeniedBusinessException("Only support roles can access risk events");
    }
    return user;
  }

  private ModerationPriority priorityFor(RiskSeverity severity) {
    return switch (severity) {
      case CRITICAL -> ModerationPriority.CRITICAL;
      case HIGH -> ModerationPriority.HIGH;
      case MEDIUM -> ModerationPriority.NORMAL;
      case LOW -> ModerationPriority.LOW;
    };
  }

  private String comment(RiskEventResolutionRequest request) {
    if (request == null || request.comment() == null || request.comment().isBlank()) {
      return null;
    }
    return request.comment().trim();
  }

  private RiskEventResponse toResponse(RiskEvent event) {
    return new RiskEventResponse(
        event.getPublicId(),
        event.getUser().getPublicId(),
        event.getTask() == null ? null : event.getTask().getPublicId(),
        event.getChat() == null ? null : event.getChat().getPublicId(),
        event.getChatMessage() == null ? null : event.getChatMessage().getPublicId(),
        event.getDeal() == null ? null : event.getDeal().getPublicId(),
        event.getRiskType().name(),
        event.getSeverity().name(),
        event.getDetectedText(),
        event.getNormalizedDetectedValue(),
        event.getStatus().name(),
        event.getCreatedAt(),
        event.getResolvedAt(),
        event.getResolvedByAdmin() == null ? null : event.getResolvedByAdmin().getPublicId(),
        event.getResolutionComment());
  }
}
