package com.handynest.marketplace;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;

@Schema(
    description =
        "Public marketplace task response. All ids are public ids; money values include an ISO currency code.")
public record MarketplaceTaskResponse(
    String publicId,
    String customerId,
    String categoryId,
    String categoryTitle,
    String title,
    String description,
    String serviceMode,
    String priceType,
    BigDecimal budgetMin,
    BigDecimal budgetMax,
    BigDecimal fixedPrice,
    String currency,
    String countryCode,
    String countryName,
    String regionId,
    String regionName,
    String cityId,
    String cityName,
    String districtId,
    String districtName,
    String addressText,
    BigDecimal latitude,
    BigDecimal longitude,
    String status,
    String publicationStatus,
    String selectedOfferId,
    String selectedPerformerId,
    String repeatOfTaskId,
    String preferredPerformerId,
    int revisionCount,
    Instant expiresAt,
    Instant acceptedAt,
    Instant createdAt,
    Instant updatedAt) {}
