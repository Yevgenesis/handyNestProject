package com.handynest.marketplace;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    description =
        "Marketplace payment mode. MVP defaults to off-platform unless configured otherwise.")
public enum PaymentMode {
  OFF_PLATFORM,
  ON_PLATFORM_ESCROW,
  ON_PLATFORM_DIRECT
}
