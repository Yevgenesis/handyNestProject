package com.handynest.performer;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record PerformerProfileResponse(
    String publicId,
    String displayName,
    String description,
    String skillsDescription,
    String countryCode,
    String countryName,
    String regionId,
    String regionName,
    String cityId,
    String cityName,
    String districtId,
    String districtName,
    int serviceRadiusKm,
    boolean worksRemotely,
    boolean worksOnsite,
    String travelFeePolicy,
    String verificationLevel,
    String verificationStatus,
    BigDecimal ratingAverage,
    long ratingCount,
    long completedTasksCount,
    long canceledTasksCount,
    long disputeCount,
    boolean available,
    boolean topPerformer,
    Instant rejectedAt,
    String rejectionReason,
    List<PerformerCategoryResponse> categories) {
  public PerformerProfileResponse {
    categories = categories == null ? List.of() : List.copyOf(categories);
  }
}
