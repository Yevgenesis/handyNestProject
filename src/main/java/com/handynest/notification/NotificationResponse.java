package com.handynest.notification;

import java.time.Instant;

public record NotificationResponse(
        String publicId,
        String type,
        String title,
        String body,
        String targetType,
        String targetId,
        Instant readAt,
        Instant deliveredAt,
        String metadataJson,
        Instant createdAt
) {
}
