package com.handynest.marketplace;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Marketplace payment transaction status.")
public enum PaymentStatus {
  CREATED,
  NOT_REQUIRED,
  PENDING,
  AUTHORIZED,
  HELD,
  CAPTURED,
  RELEASED,
  REFUNDED,
  PARTIALLY_REFUNDED,
  FAILED,
  CANCELED
}
