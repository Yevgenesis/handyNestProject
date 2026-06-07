package com.handynest.marketplace;

import java.time.Instant;

public record DisputeCaseResponse(
        String publicId,
        String dealId,
        String taskId,
        String chatId,
        String openedByUserId,
        String reason,
        String description,
        String status,
        String adminDecision,
        Instant resolvedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
