package com.handynest.marketplace;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RevisionRequest(
        @NotBlank @Size(max = 2000) String reason
) {
}
