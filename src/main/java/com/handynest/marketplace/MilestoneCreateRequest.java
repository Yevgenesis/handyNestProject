package com.handynest.marketplace;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;

public record MilestoneCreateRequest(
        @NotBlank
        @Size(max = 160)
        String title,

        @Size(max = 2000)
        String description,

        @NotNull
        @DecimalMin(value = "0.01")
        BigDecimal amount,

        @Size(min = 3, max = 3)
        String currency,

        @NotNull
        @Future
        Instant dueDate
) {
}
