package com.handynest.marketplace;

import java.math.BigDecimal;
import java.time.Instant;

public record FavoritePerformerResponse(
        String performerId,
        String displayName,
        String cityId,
        String cityName,
        BigDecimal ratingAverage,
        long ratingCount,
        boolean available,
        String note,
        Instant createdAt
) {
}
