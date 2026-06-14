package com.handynest.marketplace;

import com.handynest.catalog.category.CategoryServiceMode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;

@Schema(
    description =
        "Request to create a marketplace task. All ids are public ids; currency defaults to the task country.")
public record TaskCreateRequest(
    @NotBlank @Size(max = 160) String title,
    @NotBlank @Size(max = 4000) String description,
    @NotBlank String categoryId,
    @NotNull CategoryServiceMode serviceMode,
    @NotNull PriceType priceType,
    @DecimalMin("0.00") BigDecimal budgetMin,
    @DecimalMin("0.00") BigDecimal budgetMax,
    @DecimalMin("0.00") BigDecimal fixedPrice,
    @Size(min = 3, max = 3) String currency,
    String countryCode,
    @NotBlank String cityId,
    String districtId,
    @Size(max = 500) String addressText,
    BigDecimal latitude,
    BigDecimal longitude,
    Instant expiresAt) {}
