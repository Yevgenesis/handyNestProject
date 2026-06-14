package com.handynest.marketplace;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Deal chat availability status.")
public enum ChatStatus {
  ACTIVE,
  READ_ONLY,
  CLOSED
}
