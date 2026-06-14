package com.handynest.moderation;

import java.time.Instant;

public record ComplaintResponse(
    String publicId,
    String reporterUserId,
    String targetUserId,
    String targetType,
    String targetId,
    String reason,
    String description,
    String status,
    String moderationCaseId,
    Instant createdAt,
    Instant resolvedAt) {}
