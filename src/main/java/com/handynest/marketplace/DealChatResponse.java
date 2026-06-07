package com.handynest.marketplace;

import java.time.Instant;

public record DealChatResponse(
        String publicId,
        String dealId,
        String taskId,
        String customerId,
        String performerId,
        String performerDisplayName,
        String status,
        Instant closedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
