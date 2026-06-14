package com.handynest.marketplace;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Admin request to resolve a marketplace dispute case.")
public record DisputeResolutionRequest(
    @NotNull DisputeCaseStatus status, @Size(max = 4000) String adminDecision) {}
