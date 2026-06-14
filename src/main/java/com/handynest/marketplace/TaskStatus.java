package com.handynest.marketplace;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Marketplace task lifecycle status.")
public enum TaskStatus {
  DRAFT,
  MODERATION,
  OPEN,
  IN_PROGRESS,
  WORK_SUBMITTED,
  REVISION_REQUESTED,
  COMPLETED,
  CANCELED,
  DISPUTED,
  EXPIRED
}
