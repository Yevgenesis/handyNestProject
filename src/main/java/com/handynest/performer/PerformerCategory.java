package com.handynest.performer;

import com.handynest.catalog.category.Category;
import com.handynest.common.domain.BaseAuditEntity;
import com.handynest.identity.User;
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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
    name = "performer_category",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uc_performer_category_profile_category",
            columnNames = {"performer_profile_id", "category_id"}))
public class PerformerCategory extends BaseAuditEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "performer_profile_id", nullable = false)
  private PerformerProfile performerProfile;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "category_id", nullable = false)
  private Category category;

  @Column(name = "experience_years")
  private Integer experienceYears;

  @Column(name = "price_from", precision = 12, scale = 2)
  private BigDecimal priceFrom;

  @Column(name = "price_to", precision = 12, scale = 2)
  private BigDecimal priceTo;

  @Column(nullable = false, length = 3)
  private String currency = "UZS";

  @Column(name = "is_primary", nullable = false)
  private boolean primaryCategory;

  @Enumerated(EnumType.STRING)
  @Column(name = "approval_status", nullable = false, length = 32)
  private PerformerCategoryApprovalStatus approvalStatus =
      PerformerCategoryApprovalStatus.NOT_REQUIRED;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reviewed_by_user_id")
  private User reviewedBy;

  @Column(name = "reviewed_at")
  private Instant reviewedAt;

  @Column(name = "rejection_reason", length = 1000)
  private String rejectionReason;

  protected PerformerCategory() {}

  public PerformerCategory(Category category) {
    this.category = category;
    this.approvalStatus =
        requiresManualReview(category)
            ? PerformerCategoryApprovalStatus.PENDING
            : PerformerCategoryApprovalStatus.NOT_REQUIRED;
  }

  public Long getId() {
    return id;
  }

  public PerformerProfile getPerformerProfile() {
    return performerProfile;
  }

  void setPerformerProfile(PerformerProfile performerProfile) {
    this.performerProfile = performerProfile;
  }

  public Category getCategory() {
    return category;
  }

  public Integer getExperienceYears() {
    return experienceYears;
  }

  public void setExperienceYears(Integer experienceYears) {
    this.experienceYears = experienceYears;
  }

  public BigDecimal getPriceFrom() {
    return priceFrom;
  }

  public void setPriceFrom(BigDecimal priceFrom) {
    this.priceFrom = priceFrom;
  }

  public BigDecimal getPriceTo() {
    return priceTo;
  }

  public void setPriceTo(BigDecimal priceTo) {
    this.priceTo = priceTo;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public boolean isPrimaryCategory() {
    return primaryCategory;
  }

  public void setPrimaryCategory(boolean primaryCategory) {
    this.primaryCategory = primaryCategory;
  }

  public PerformerCategoryApprovalStatus getApprovalStatus() {
    return approvalStatus;
  }

  public User getReviewedBy() {
    return reviewedBy;
  }

  public Instant getReviewedAt() {
    return reviewedAt;
  }

  public String getRejectionReason() {
    return rejectionReason;
  }

  public boolean isApprovedForServing() {
    if (requiresManualReview(category)) {
      return approvalStatus == PerformerCategoryApprovalStatus.APPROVED;
    }
    return true;
  }

  public void approve(User admin, Instant reviewedAt) {
    this.approvalStatus = PerformerCategoryApprovalStatus.APPROVED;
    this.reviewedBy = admin;
    this.reviewedAt = reviewedAt;
    this.rejectionReason = null;
  }

  public void reject(User admin, String reason, Instant reviewedAt) {
    this.approvalStatus = PerformerCategoryApprovalStatus.REJECTED;
    this.reviewedBy = admin;
    this.reviewedAt = reviewedAt;
    this.rejectionReason = reason;
  }

  public void preserveModerationFrom(PerformerCategory previous) {
    if (!requiresManualReview(category)) {
      this.approvalStatus = PerformerCategoryApprovalStatus.NOT_REQUIRED;
      return;
    }
    if (previous.approvalStatus == PerformerCategoryApprovalStatus.NOT_REQUIRED) {
      this.approvalStatus = PerformerCategoryApprovalStatus.PENDING;
      return;
    }
    this.approvalStatus = previous.approvalStatus;
    this.reviewedBy = previous.reviewedBy;
    this.reviewedAt = previous.reviewedAt;
    this.rejectionReason = previous.rejectionReason;
  }

  public void requireFreshApproval() {
    this.approvalStatus = PerformerCategoryApprovalStatus.PENDING;
    this.reviewedBy = null;
    this.reviewedAt = null;
    this.rejectionReason = null;
  }

  public void removeApprovalRequirement() {
    this.approvalStatus = PerformerCategoryApprovalStatus.NOT_REQUIRED;
    this.reviewedBy = null;
    this.reviewedAt = null;
    this.rejectionReason = null;
  }

  private boolean requiresManualReview(Category category) {
    return category.isRequiresManualApproval() || category.isRequiresLicense();
  }
}
