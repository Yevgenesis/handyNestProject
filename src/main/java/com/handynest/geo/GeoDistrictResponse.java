package com.handynest.geo;

import java.math.BigDecimal;

public record GeoDistrictResponse(
    String publicId,
    String name,
    String slug,
    BigDecimal latitude,
    BigDecimal longitude,
    boolean supported) {}
