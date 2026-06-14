package com.handynest.performer;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Admin review state for performer access to a risk category.")
public record PerformerCategoryModerationResponse(
    String performerId,
    String categoryId,
    String categoryTitle,
    PerformerCategoryApprovalStatus approvalStatus,
    String reviewedByUserId,
    Instant reviewedAt,
    String rejectionReason) {}
