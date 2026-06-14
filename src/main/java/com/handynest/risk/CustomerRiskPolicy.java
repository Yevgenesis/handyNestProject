package com.handynest.risk;

import com.handynest.common.error.AccessDeniedBusinessException;
import com.handynest.common.error.VerificationRequiredException;
import com.handynest.identity.User;
import com.handynest.marketplace.MarketplaceTaskRepository;
import com.handynest.marketplace.TaskStatus;
import com.handynest.platform.PlatformSettingKey;
import com.handynest.platform.PlatformSettingService;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomerRiskPolicy {

  private static final List<TaskStatus> ACTIVE_TASK_STATUSES =
      List.of(
          TaskStatus.MODERATION,
          TaskStatus.OPEN,
          TaskStatus.IN_PROGRESS,
          TaskStatus.WORK_SUBMITTED,
          TaskStatus.REVISION_REQUESTED,
          TaskStatus.DISPUTED);

  private final CustomerRiskProperties properties;
  private final MarketplaceTaskRepository taskRepository;
  private final PlatformSettingService platformSettingService;

  public void assertTaskCreationAllowed(User customer, BigDecimal taskAmount) {
    if (platformSettingService.booleanValue(PlatformSettingKey.CUSTOMER_RISK_BLOCKING_ENABLED)) {
      BigDecimal score =
          customer.getCustomerRiskScore() == null
              ? BigDecimal.ZERO
              : customer.getCustomerRiskScore();
      if (score.compareTo(properties.getTaskCreationBlockThreshold()) >= 0) {
        throw new AccessDeniedBusinessException(
            "New task creation is restricted pending risk review");
      }
    }
    long activeTasks =
        taskRepository.countByCustomerIdAndStatusIn(customer.getId(), ACTIVE_TASK_STATUSES);
    if (activeTasks >= platformSettingService.integerValue(PlatformSettingKey.MAX_ACTIVE_TASKS)) {
      throw new AccessDeniedBusinessException("Active task limit reached");
    }
    if (taskAmount != null
        && taskAmount.compareTo(properties.getHighValueTaskThreshold()) >= 0
        && !customer.isPhoneVerified()) {
      throw new VerificationRequiredException(
          "Phone verification is required for high-value tasks");
    }
  }
}
