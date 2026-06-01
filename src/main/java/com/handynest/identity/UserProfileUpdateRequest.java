package com.handynest.identity;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UserProfileUpdateRequest(
        @Size(max = 50)
        String firstName,

        @Size(max = 50)
        String lastName,

        @Size(max = 32)
        String phone,

        AccountType accountType,

        String countryCode,

        String cityId,

        String districtId,

        @Min(0)
        @Max(500)
        Integer preferredServiceRadiusKm,

        @Valid
        BusinessProfileRequest businessProfile
) {
}
