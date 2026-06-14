package com.handynest.risk;

import jakarta.validation.constraints.Size;

public record RiskEventResolutionRequest(@Size(max = 1000) String comment) {}
