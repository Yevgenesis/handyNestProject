package com.handynest.marketplace;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(
    description =
        "Chat message response. All ids are public ids; timestamps are ISO-8601 instants.")
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
    String riskWarning) {}
