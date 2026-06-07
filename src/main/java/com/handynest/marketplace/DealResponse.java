package com.handynest.marketplace;

import java.time.Instant;

public record DealResponse(
        String publicId,
        String taskId,
        String customerId,
        String performerId,
        String performerDisplayName,
        String acceptedOfferId,
        String chatId,
        String status,
        String paymentMode,
        String paymentStatus,
        String contactVisibilityStatus,
        boolean milestoneEnabled,
        int revisionCount,
        Instant completedAt,
        Instant canceledAt,
        Instant createdAt,
        Instant updatedAt
) {
}
