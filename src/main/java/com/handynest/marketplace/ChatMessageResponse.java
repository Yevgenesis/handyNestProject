package com.handynest.marketplace;

import java.time.Instant;

public record ChatMessageResponse(
        String publicId,
        String chatId,
        String senderId,
        String senderDisplayName,
        String messageType,
        String text,
        String systemCode,
        String attachmentId,
        Instant createdAt,
        Instant readAt,
        boolean riskFlag,
        String riskWarning
) {
}
