package com.handynest.marketplace;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Optional reason/comment for milestone lifecycle decisions.")
public record MilestoneDecisionRequest(@Size(max = 1000) String reason) {}
