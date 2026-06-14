package com.handynest.performer;

import jakarta.validation.constraints.NotNull;

public record PerformerAvailabilityRequest(@NotNull Boolean available) {}
