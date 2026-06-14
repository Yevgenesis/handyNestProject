package com.handynest.marketplace;

import jakarta.validation.constraints.Size;

public record DisputeReviewRequest(@Size(max = 1000) String comment) {}
