package com.handynest.marketplace;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "Request to create a performer offer for a marketplace task.")
public record TaskOfferCreateRequest(
    @NotBlank @Size(max = 2000) String message,
    @NotNull @DecimalMin("0.00") BigDecimal proposedPrice,
    @Size(min = 3, max = 3) String currency,
    @Size(max = 120) String estimatedDuration,
    Boolean includesMaterials,
    Instant expiresAt) {}
