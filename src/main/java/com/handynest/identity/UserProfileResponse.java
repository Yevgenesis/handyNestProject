package com.handynest.identity;

import java.math.BigDecimal;
import java.util.Set;

public record UserProfileResponse(
        String publicId,
        String email,
        String phone,
        String firstName,
        String lastName,
        boolean emailVerified,
        boolean phoneVerified,
        String status,
        String accountType,
        Set<String> roles,
        String countryCode,
        String countryName,
        String cityId,
        String cityName,
        String districtId,
        String districtName,
        Integer preferredServiceRadiusKm,
        BigDecimal customerRiskScore,
        CustomerProfileResponse customerProfile,
        BusinessProfileResponse businessProfile
) {
    public UserProfileResponse {
        roles = roles == null ? Set.of() : Set.copyOf(roles);
    }
}
