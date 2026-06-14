package com.handynest.marketplace;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Deal response created from an accepted task offer. All ids are public ids.")
public record DealResponse(
    String publicId,
    String taskId,
    String taskTitle,
    String categoryId,
    String categoryTitle,
    String cityId,
    String cityName,
    String customerId,
    String customerDisplayName,
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
    String canceledByUserId,
    DealCancelReason cancelReason,
    String cancelComment,
    boolean canceledAfterContactReveal,
    Instant createdAt,
    Instant updatedAt) {}
