package com.handynest.marketplace;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Lifecycle status for a performer task offer.")
public enum TaskOfferStatus {
  PENDING,
  ACCEPTED,
  REJECTED,
  CANCELED,
  EXPIRED
}
