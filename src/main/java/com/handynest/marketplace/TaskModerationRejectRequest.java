package com.handynest.marketplace;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Admin moderation rejection request for a marketplace task.")
public record TaskModerationRejectRequest(@Size(max = 1000) String reason) {}
