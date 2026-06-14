package com.handynest.performer;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Manual approval state for a performer-category assignment.")
public enum PerformerCategoryApprovalStatus {
  NOT_REQUIRED,
  PENDING,
  APPROVED,
  REJECTED
}
