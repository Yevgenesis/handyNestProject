package com.handynest.marketplace;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Task or offer pricing model.")
public enum PriceType {
  FIXED,
  HOURLY,
  NEGOTIABLE
}
