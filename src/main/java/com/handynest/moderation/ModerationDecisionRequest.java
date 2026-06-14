package com.handynest.moderation;

import jakarta.validation.constraints.Size;

public record ModerationDecisionRequest(@Size(max = 1000) String comment) {}
