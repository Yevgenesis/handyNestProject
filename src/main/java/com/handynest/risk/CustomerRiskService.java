package com.handynest.risk;

import com.handynest.identity.User;
import com.handynest.identity.UserRepository;
import com.handynest.moderation.Complaint;
import com.handynest.moderation.ModerationCase;
import com.handynest.moderation.ModerationCaseRepository;
import com.handynest.moderation.ModerationCaseStatus;
import com.handynest.moderation.ModerationPriority;
import com.handynest.moderation.ModerationTargetType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerRiskService {

  private final CustomerRiskAdjustmentRepository adjustmentRepository;
  private final CustomerRiskProperties properties;
  private final UserRepository userRepository;
  private final ModerationCaseRepository moderationCaseRepository;

  @Transactional
  public void recordRiskEvent(RiskEvent event) {
    apply(
        event.getUser(),
        CustomerRiskSourceType.RISK_EVENT,
        event.getPublicId(),
        pointsFor(event.getSeverity()),
        "Risk event: " + event.getRiskType().name());
  }

  @Transactional
  public void reverseRiskEvent(RiskEvent event) {
    reverse(CustomerRiskSourceType.RISK_EVENT, event.getPublicId());
  }

  @Transactional
  public void recordConfirmedComplaint(Complaint complaint) {
    if (complaint.getTargetUser() == null) {
      return;
    }
    apply(
        complaint.getTargetUser(),
        CustomerRiskSourceType.COMPLAINT,
        complaint.getPublicId(),
        properties.getConfirmedComplaintPoints(),
        "Confirmed complaint: " + complaint.getReason());
  }

  private void apply(
      User user,
      CustomerRiskSourceType sourceType,
      String sourcePublicId,
      BigDecimal points,
      String reason) {
    if (adjustmentRepository
        .findBySourceTypeAndSourcePublicId(sourceType, sourcePublicId)
        .isPresent()) {
      return;
    }
    User lockedUser = userRepository.findByIdForUpdate(user.getId()).orElseThrow();
    adjustmentRepository.save(
        new CustomerRiskAdjustment(lockedUser, sourceType, sourcePublicId, points, reason));
    lockedUser.addCustomerRiskScore(points);
    createProfileModerationCaseIfNeeded(lockedUser);
  }

  private void reverse(CustomerRiskSourceType sourceType, String sourcePublicId) {
    CustomerRiskAdjustment adjustment =
        adjustmentRepository
            .findBySourceTypeAndSourcePublicId(sourceType, sourcePublicId)
            .orElse(null);
    if (adjustment == null || !adjustment.isActive()) {
      return;
    }
    User lockedUser = userRepository.findByIdForUpdate(adjustment.getUser().getId()).orElseThrow();
    adjustment.reverse(Instant.now());
    lockedUser.subtractCustomerRiskScore(adjustment.getPoints());
  }

  private void createProfileModerationCaseIfNeeded(User user) {
    if (user.getCustomerRiskScore().compareTo(properties.getModerationThreshold()) < 0) {
      return;
    }
    if (moderationCaseRepository.existsByTargetTypeAndTargetIdAndStatusIn(
        ModerationTargetType.PROFILE,
        user.getPublicId(),
        List.of(ModerationCaseStatus.OPEN, ModerationCaseStatus.IN_REVIEW))) {
      return;
    }
    moderationCaseRepository.save(
        new ModerationCase(
            ModerationTargetType.PROFILE,
            user.getPublicId(),
            null,
            "Customer risk score reached " + user.getCustomerRiskScore().toPlainString(),
            priorityFor(user.getCustomerRiskScore())));
  }

  private BigDecimal pointsFor(RiskSeverity severity) {
    return switch (severity) {
      case LOW -> properties.getLowSeverityPoints();
      case MEDIUM -> properties.getMediumSeverityPoints();
      case HIGH -> properties.getHighSeverityPoints();
      case CRITICAL -> properties.getCriticalSeverityPoints();
    };
  }

  private ModerationPriority priorityFor(BigDecimal score) {
    return score.compareTo(properties.getTaskCreationBlockThreshold()) >= 0
        ? ModerationPriority.CRITICAL
        : ModerationPriority.HIGH;
  }
}
