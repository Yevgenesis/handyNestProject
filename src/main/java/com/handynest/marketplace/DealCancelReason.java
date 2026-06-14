package com.handynest.marketplace;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Participant-provided reason for canceling an active deal.")
public enum DealCancelReason {
  CUSTOMER_CHANGED_MIND,
  PERFORMER_NOT_RESPONDING,
  CUSTOMER_NOT_RESPONDING,
  PRICE_NOT_ACCEPTED,
  SCHEDULE_NOT_ACCEPTED,
  TASK_NO_LONGER_ACTUAL,
  WRONG_PERFORMER_SELECTED,
  SAFETY_CONCERN,
  DUPLICATE_TASK,
  OTHER
}
