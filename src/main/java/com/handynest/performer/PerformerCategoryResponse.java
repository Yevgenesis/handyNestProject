package com.handynest.performer;

import java.math.BigDecimal;

public record PerformerCategoryResponse(
        String categoryId,
        String title,
        Integer experienceYears,
        BigDecimal priceFrom,
        BigDecimal priceTo,
        String currency,
        boolean primary
) {
}
