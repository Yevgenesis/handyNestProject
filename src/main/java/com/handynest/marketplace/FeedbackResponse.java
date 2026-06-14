package com.handynest.marketplace;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Marketplace feedback response. All ids are public ids.")
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
    Instant createdAt) {}
