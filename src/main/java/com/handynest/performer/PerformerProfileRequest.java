package com.handynest.performer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record PerformerProfileRequest(
    @NotBlank @Size(max = 120) String displayName,
    @Size(max = 2000) String description,
    @Size(max = 2000) String skillsDescription,
    String countryCode,
    String cityId,
    String districtId,
    @Min(0) @Max(500) Integer serviceRadiusKm,
    Boolean worksRemotely,
    Boolean worksOnsite,
    @Size(max = 500) String travelFeePolicy,
    @Valid List<PerformerCategoryRequest> categories) {}
