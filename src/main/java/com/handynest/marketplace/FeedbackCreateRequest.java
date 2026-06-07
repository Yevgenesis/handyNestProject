package com.handynest.marketplace;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record FeedbackCreateRequest(
        @Min(1)
        @Max(5)
        int grade,

        @Size(max = 2000)
        String text
) {
}
