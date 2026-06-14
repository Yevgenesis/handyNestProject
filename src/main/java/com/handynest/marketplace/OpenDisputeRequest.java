package com.handynest.marketplace;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to open a dispute for a deal chat.")
public record OpenDisputeRequest(
    @NotBlank @Size(max = 160) String reason, @Size(max = 4000) String description) {}
