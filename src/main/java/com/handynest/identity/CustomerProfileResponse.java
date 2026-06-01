package com.handynest.identity;

import java.math.BigDecimal;

public record CustomerProfileResponse(
        BigDecimal ratingAverage,
        long ratingCount,
        long completedOrdersCount,
        long canceledOrdersCount,
        long disputeCount,
        long noShowCount
) {
}
