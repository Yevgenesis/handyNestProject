package com.handynest.marketplace;

import java.math.BigDecimal;
import java.time.Instant;

public record TaskOfferResponse(
        String publicId,
        String taskId,
        String performerId,
        String performerDisplayName,
        String message,
        BigDecimal proposedPrice,
        String currency,
        String estimatedDuration,
        boolean includesMaterials,
        String status,
        Instant expiresAt,
        Instant acceptedAt,
        Instant rejectedAt,
        Instant canceledAt,
        Instant createdAt,
        Instant updatedAt
) {
}
