package com.handynest.geo;

import java.math.BigDecimal;

public record GeoCityResponse(
    String publicId,
    String name,
    String slug,
    BigDecimal latitude,
    BigDecimal longitude,
    boolean supported,
    int sortOrder) {}
