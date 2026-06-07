package com.handynest.marketplace;

import java.time.Instant;

public record FeedbackResponse(
        String publicId,
        String taskId,
        String dealId,
        String senderId,
        String senderDisplayName,
        String receiverId,
        String receiverDisplayName,
        String performerId,
        int grade,
        String text,
        boolean hiddenByAdmin,
        String moderationStatus,
        Instant createdAt
) {
}
