package com.handynest.marketplace;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatMessageRequest(
        @NotBlank
        @Size(max = 4000)
        String text
) {
}
