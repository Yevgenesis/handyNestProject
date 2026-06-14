package com.handynest.marketplace;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "Favorite performer summary using performer and geo publicIds.")
public record FavoritePerformerResponse(
    String performerId,
    String displayName,
    String cityId,
    String cityName,
    BigDecimal ratingAverage,
    long ratingCount,
    boolean available,
    String note,
    Instant createdAt) {}
