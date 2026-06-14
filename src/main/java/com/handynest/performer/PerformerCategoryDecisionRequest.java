package com.handynest.performer;

import jakarta.validation.constraints.Size;

public record PerformerCategoryDecisionRequest(@Size(max = 1000) String reason) {}
