package com.handynest.performer;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record PerformerCategoryRequest(
    @NotBlank String categoryId,
    @Min(0) Integer experienceYears,
    @DecimalMin("0.00") BigDecimal priceFrom,
    @DecimalMin("0.00") BigDecimal priceTo,
    @Size(min = 3, max = 3) String currency,
    Boolean primary) {}
