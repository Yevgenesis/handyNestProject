package com.handynest.marketplace;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "Task offer response for performer proposals. All ids are public ids.")
public record TaskOfferResponse(
    String publicId,
    String taskId,
    String taskTitle,
    String categoryId,
    String categoryTitle,
    String cityId,
    String cityName,
    String performerId,
    String performerDisplayName,
    BigDecimal performerRatingAverage,
    long performerRatingCount,
    String performerVerificationLevel,
    String message,
    BigDecimal proposedPrice,
    String currency,
    String estimatedDuration,
    boolean includesMaterials,
    String status,
    Instant expiresAt,
    Instant acceptedAt,
    Instant rejectedAt,
    Instant canceledAt,
    Instant createdAt,
    Instant updatedAt) {}
