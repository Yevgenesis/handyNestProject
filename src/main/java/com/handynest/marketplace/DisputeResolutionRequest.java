package com.handynest.marketplace;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DisputeResolutionRequest(
        @NotNull
        DisputeCaseStatus status,

        @Size(max = 4000)
        String adminDecision
) {
}
