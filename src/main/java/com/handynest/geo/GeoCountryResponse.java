package com.handynest.geo;

public record GeoCountryResponse(
    String code, String name, String phoneCode, String currencyCode, boolean supported) {}
