package com.handynest.marketplace;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Transactional deal lifecycle status.")
public enum DealStatus {
  ACTIVE,
  WORK_SUBMITTED,
  REVISION_REQUESTED,
  COMPLETED,
  DISPUTED,
  CANCELED
}
