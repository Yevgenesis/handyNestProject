package com.handynest.marketplace;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Marketplace dispute case DTO with publicId references only.")
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
    Instant updatedAt) {}
