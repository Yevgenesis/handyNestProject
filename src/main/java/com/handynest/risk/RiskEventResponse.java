package com.handynest.risk;

import java.time.Instant;

public record RiskEventResponse(
    String publicId,
    String userId,
    String taskId,
    String chatId,
    String chatMessageId,
    String dealId,
    String riskType,
    String severity,
    String detectedText,
    String normalizedDetectedValue,
    String status,
    Instant createdAt,
    Instant resolvedAt,
    String resolvedByAdminId,
    String resolutionComment) {}
