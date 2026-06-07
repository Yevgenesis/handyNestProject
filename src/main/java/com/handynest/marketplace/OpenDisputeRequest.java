package com.handynest.marketplace;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OpenDisputeRequest(
        @NotBlank
        @Size(max = 160)
        String reason,

        @Size(max = 4000)
        String description
) {
}
