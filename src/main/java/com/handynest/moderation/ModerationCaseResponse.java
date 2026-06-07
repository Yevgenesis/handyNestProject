package com.handynest.moderation;

import java.time.Instant;

public record ModerationCaseResponse(
        String publicId,
        String targetType,
        String targetId,
        String openedByUserId,
        String assignedAdminId,
        String reason,
        String status,
        String priority,
        String decision,
        String decisionComment,
        Instant createdAt,
        Instant updatedAt,
        Instant resolvedAt
) {
}
