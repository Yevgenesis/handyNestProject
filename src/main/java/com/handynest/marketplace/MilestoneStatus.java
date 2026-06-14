package com.handynest.marketplace;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Deal milestone lifecycle status.")
public enum MilestoneStatus {
  PENDING,
  IN_PROGRESS,
  SUBMITTED,
  ACCEPTED,
  REJECTED,
  DISPUTED,
  CANCELED
}
