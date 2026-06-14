package com.handynest.performer;

import com.handynest.common.domain.PublicIdEntity;
import com.handynest.geo.City;
import com.handynest.geo.Country;
import com.handynest.geo.District;
import com.handynest.geo.Region;
import com.handynest.identity.User;
import com.handynest.identity.VerificationStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "performer_profile")
public class PerformerProfile extends PublicIdEntity {

  @Getter
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Version
  @Column(nullable = false)
  private long version;

  @Getter
  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private User user;

  @Setter
  @Getter
  @Column(name = "display_name", nullable = false, length = 120)
  private String displayName;

  @Setter
  @Getter
  @Column(length = 2000)
  private String description;

  @Setter
  @Getter
  @Column(name = "skills_description", length = 2000)
  private String skillsDescription;

  @Setter
  @Getter
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "base_country_id", nullable = false)
  private Country baseCountry;

  @Setter
  @Getter
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "base_region_id")
  private Region baseRegion;

  @Setter
  @Getter
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "base_city_id", nullable = false)
  private City baseCity;

  @Setter
  @Getter
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "base_district_id")
  private District baseDistrict;

  @Setter
  @Getter
  @Column(name = "service_radius_km", nullable = false)
  private int serviceRadiusKm;

  @Setter
  @Getter
  @Column(name = "works_remotely", nullable = false)
  private boolean worksRemotely;

  @Setter
  @Getter
  @Column(name = "works_onsite", nullable = false)
  private boolean worksOnsite;

  @Setter
  @Getter
  @Column(name = "travel_fee_policy", length = 500)
  private String travelFeePolicy;

  @Getter
  @Enumerated(EnumType.STRING)
  @Column(name = "verification_level", nullable = false, length = 32)
  private PerformerVerificationLevel verificationLevel = PerformerVerificationLevel.NONE;

  @Getter
  @Enumerated(EnumType.STRING)
  @Column(name = "verification_status", nullable = false, length = 32)
  private VerificationStatus verificationStatus = VerificationStatus.NOT_SUBMITTED;

  @Getter
  @Column(name = "rating_average", nullable = false, precision = 3, scale = 2)
  private BigDecimal ratingAverage = BigDecimal.ZERO;

  @Getter
  @Column(name = "rating_count", nullable = false)
  private long ratingCount;

  @Getter
  @Column(name = "completed_tasks_count", nullable = false)
  private long completedTasksCount;

  @Getter
  @Column(name = "canceled_tasks_count", nullable = false)
  private long canceledTasksCount;

  @Getter
  @Column(name = "dispute_count", nullable = false)
  private long disputeCount;

  @Setter
  @Getter
  @Column(name = "is_available", nullable = false)
  private boolean available;

  @Getter
  @Column(name = "is_top_performer", nullable = false)
  private boolean topPerformer;

  @Getter
  @Column(name = "approved_at")
  private Instant approvedAt;

  @Getter
  @Column(name = "rejected_at")
  private Instant rejectedAt;

  @Getter
  @Column(name = "rejection_reason", length = 1000)
  private String rejectionReason;

  @Getter
  @OneToMany(mappedBy = "performerProfile", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<PerformerCategory> categories = new LinkedHashSet<>();

  protected PerformerProfile() {}

  public PerformerProfile(User user) {
    this.user = user;
  }

  public void replaceCategories(Set<PerformerCategory> categories) {
    this.categories.clear();
    categories.forEach(
        category -> {
          category.setPerformerProfile(this);
          this.categories.add(category);
        });
  }

  public void applyRating(int grade, long previousRatingCount) {
    BigDecimal previousTotal = ratingAverage.multiply(BigDecimal.valueOf(previousRatingCount));
    this.ratingAverage =
        previousTotal
            .add(BigDecimal.valueOf(grade))
            .divide(BigDecimal.valueOf(previousRatingCount + 1), 2, RoundingMode.HALF_UP);
    this.ratingCount = previousRatingCount + 1;
  }

  public void markVerificationPending() {
    this.verificationStatus = VerificationStatus.PENDING;
    this.rejectedAt = null;
    this.rejectionReason = null;
  }

  public void approveVerification(PerformerVerificationLevel level, Instant reviewedAt) {
    if (level.ordinal() > this.verificationLevel.ordinal()) {
      this.verificationLevel = level;
    }
    this.verificationStatus = VerificationStatus.APPROVED;
    this.approvedAt = reviewedAt;
    this.rejectedAt = null;
    this.rejectionReason = null;
  }

  public void markPhoneVerified(Instant verifiedAt) {
    if (verificationLevel == PerformerVerificationLevel.NONE) {
      this.verificationLevel = PerformerVerificationLevel.PHONE_VERIFIED;
    }
    if (verificationStatus != VerificationStatus.PENDING) {
      this.verificationStatus = VerificationStatus.APPROVED;
      this.approvedAt = verifiedAt;
      this.rejectedAt = null;
      this.rejectionReason = null;
    }
  }

  public void rejectVerification(String reason, Instant reviewedAt) {
    this.verificationStatus = VerificationStatus.REJECTED;
    this.rejectedAt = reviewedAt;
    this.rejectionReason = reason;
  }

  public PerformerVerificationLevel getEffectiveVerificationLevel() {
    if (!user.isPhoneVerified()) {
      return PerformerVerificationLevel.NONE;
    }
    return verificationLevel == PerformerVerificationLevel.NONE
        ? PerformerVerificationLevel.PHONE_VERIFIED
        : verificationLevel;
  }
}
