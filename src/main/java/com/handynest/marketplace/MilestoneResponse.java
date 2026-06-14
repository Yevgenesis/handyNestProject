package com.handynest.marketplace;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "Deal milestone DTO using publicId strings and ISO timestamps.")
public record MilestoneResponse(
    String publicId,
    String dealId,
    String taskId,
    String title,
    String description,
    BigDecimal amount,
    String currency,
    Instant dueDate,
    String status,
    Instant submittedAt,
    Instant acceptedAt,
    Instant rejectedAt,
    Instant createdAt,
    Instant updatedAt) {}
