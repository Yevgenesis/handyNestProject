package com.handynest.marketplace;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Optional customer message when accepting submitted work.")
public record WorkAcceptanceRequest(@Size(max = 2000) String message) {}
