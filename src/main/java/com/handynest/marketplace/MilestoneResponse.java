package com.handynest.marketplace;

import java.math.BigDecimal;
import java.time.Instant;

public record MilestoneResponse(
        String publicId,
        String dealId,
        String taskId,
        String title,
        String description,
        BigDecimal amount,
        String currency,
        Instant dueDate,
        String status,
        Instant submittedAt,
        Instant acceptedAt,
        Instant rejectedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
