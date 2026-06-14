package com.handynest.marketplace;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(
    description =
        "Deal chat response for customer-performer communication. All ids are public ids.")
public record DealChatResponse(
    String publicId,
    String dealId,
    String taskId,
    String taskTitle,
    String customerId,
    String customerDisplayName,
    String performerId,
    String performerDisplayName,
    String participantRole,
    String status,
    String lastMessageText,
    String lastMessageType,
    Instant lastMessageAt,
    long unreadCount,
    Instant closedAt,
    Instant createdAt,
    Instant updatedAt) {}
