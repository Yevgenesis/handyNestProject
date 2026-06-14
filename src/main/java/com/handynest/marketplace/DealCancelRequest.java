package com.handynest.marketplace;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DealCancelRequest(
    @NotNull DealCancelReason reason, @Size(max = 1000) String comment) {}
