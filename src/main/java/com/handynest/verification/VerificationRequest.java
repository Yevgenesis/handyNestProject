package com.handynest.verification;

import com.handynest.common.domain.PublicIdEntity;
import com.handynest.identity.User;
import com.handynest.marketplace.MarketplaceAttachment;
import com.handynest.performer.PerformerProfile;
import com.handynest.performer.PerformerVerificationLevel;
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
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;

@Entity
@Table(name = "verification_request")
public class VerificationRequest extends PublicIdEntity {

  @Getter
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Version
  @Column(nullable = false)
  private long version;

  @Getter
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "performer_profile_id", nullable = false)
  private PerformerProfile performerProfile;

  @Getter
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "requested_by_user_id", nullable = false)
  private User requestedBy;

  @Getter
  @Enumerated(EnumType.STRING)
  @Column(name = "requested_level", nullable = false, length = 32)
  private PerformerVerificationLevel requestedLevel;

  @Getter
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private VerificationRequestStatus status = VerificationRequestStatus.PENDING;

  @Getter
  @Column(length = 1000)
  private String comment;

  @Getter
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reviewed_by_user_id")
  private User reviewedBy;

  @Getter
  @Column(name = "reviewed_at")
  private Instant reviewedAt;

  @Getter
  @Column(name = "rejection_reason", length = 1000)
  private String rejectionReason;

  @Getter
  @OneToMany(mappedBy = "verificationRequest", cascade = CascadeType.ALL)
  private Set<MarketplaceAttachment> documents = new LinkedHashSet<>();

  protected VerificationRequest() {}

  public VerificationRequest(
      PerformerProfile performerProfile,
      User requestedBy,
      PerformerVerificationLevel requestedLevel,
      String comment) {
    this.performerProfile = performerProfile;
    this.requestedBy = requestedBy;
    this.requestedLevel = requestedLevel;
    this.comment = comment;
  }

  public void addDocument(MarketplaceAttachment document) {
    document.attachToVerificationRequest(this);
    documents.add(document);
  }

  public void approve(User admin, Instant reviewedAt) {
    this.status = VerificationRequestStatus.APPROVED;
    this.reviewedBy = admin;
    this.reviewedAt = reviewedAt;
    this.rejectionReason = null;
  }

  public void reject(User admin, String reason, Instant reviewedAt) {
    this.status = VerificationRequestStatus.REJECTED;
    this.reviewedBy = admin;
    this.reviewedAt = reviewedAt;
    this.rejectionReason = reason;
  }
}
