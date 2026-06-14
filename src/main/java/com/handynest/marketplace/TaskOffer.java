package com.handynest.marketplace;

import com.handynest.common.domain.PublicIdEntity;
import com.handynest.common.error.InvalidStatusTransitionException;
import com.handynest.performer.PerformerProfile;
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
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "task_offer")
public class TaskOffer extends PublicIdEntity {

  @Getter
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Version
  @Column(nullable = false)
  private long version;

  @Getter
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "task_id", nullable = false)
  private MarketplaceTask task;

  @Getter
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "performer_id", nullable = false)
  private PerformerProfile performer;

  @Setter
  @Getter
  @Column(nullable = false, length = 2000)
  private String message;

  @Setter
  @Getter
  @Column(name = "proposed_price", nullable = false, precision = 19, scale = 2)
  private BigDecimal proposedPrice;

  @Setter
  @Getter
  @Column(nullable = false, length = 3)
  private String currency = "UZS";

  @Setter
  @Getter
  @Column(name = "estimated_duration", length = 120)
  private String estimatedDuration;

  @Setter
  @Getter
  @Column(name = "includes_materials", nullable = false)
  private boolean includesMaterials;

  @Getter
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private TaskOfferStatus status = TaskOfferStatus.PENDING;

  @Setter
  @Getter
  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Getter
  @Column(name = "expired_at")
  private Instant expiredAt;

  @Getter
  @Column(name = "accepted_at")
  private Instant acceptedAt;

  @Getter
  @Column(name = "rejected_at")
  private Instant rejectedAt;

  @Getter
  @Column(name = "canceled_at")
  private Instant canceledAt;

  protected TaskOffer() {}

  public TaskOffer(MarketplaceTask task, PerformerProfile performer) {
    this.task = task;
    this.performer = performer;
  }

  public void accept(Instant acceptedAt) {
    requireOfferStatus(TaskOfferStatus.PENDING);
    this.status = TaskOfferStatus.ACCEPTED;
    this.acceptedAt = acceptedAt;
  }

  public void reject(Instant rejectedAt) {
    requireOfferStatus(TaskOfferStatus.PENDING);
    this.status = TaskOfferStatus.REJECTED;
    this.rejectedAt = rejectedAt;
  }

  public void cancel(Instant canceledAt) {
    requireOfferStatus(TaskOfferStatus.PENDING);
    this.status = TaskOfferStatus.CANCELED;
    this.canceledAt = canceledAt;
  }

  public void expire(Instant expiredAt) {
    requireOfferStatus(TaskOfferStatus.PENDING);
    this.status = TaskOfferStatus.EXPIRED;
    this.expiredAt = expiredAt;
  }

  private void requireOfferStatus(TaskOfferStatus... allowedStatuses) {
    for (TaskOfferStatus allowedStatus : allowedStatuses) {
      if (this.status == allowedStatus) {
        return;
      }
    }
    throw new InvalidStatusTransitionException(
        "TaskOffer", this.status.name(), allowedStatuses[0].name());
  }
}
