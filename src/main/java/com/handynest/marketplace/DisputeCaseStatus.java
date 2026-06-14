package com.handynest.marketplace;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Marketplace dispute case status.")
public enum DisputeCaseStatus {
  OPEN,
  UNDER_REVIEW,
  WAITING_FOR_CUSTOMER,
  WAITING_FOR_PERFORMER,
  RESOLVED_REFUND,
  RESOLVED_RELEASE,
  RESOLVED_PARTIAL,
  CANCELED
}
