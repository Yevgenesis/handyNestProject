package com.handynest.marketplace;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to leave feedback after a completed marketplace deal.")
public record FeedbackCreateRequest(@Min(1) @Max(5) int grade, @Size(max = 2000) String text) {}
