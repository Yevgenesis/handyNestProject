package com.handynest.marketplace;

import com.handynest.common.domain.PublicIdEntity;
import com.handynest.common.error.InvalidStatusTransitionException;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import lombok.Getter;

@Entity
@Table(name = "dispute_case")
public class DisputeCase extends PublicIdEntity {

  @Getter
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Version
  @Column(nullable = false)
  private long version;

  @Getter
  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "deal_id", nullable = false)
  private Deal deal;

  @Getter
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "task_id", nullable = false)
  private MarketplaceTask task;

  @Getter
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "opened_by_user_id", nullable = false)
  private User openedByUser;

  @Getter
  @Column(nullable = false, length = 160)
  private String reason;

  @Getter
  @Column(length = 4000)
  private String description;

  @Getter
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private DisputeCaseStatus status = DisputeCaseStatus.OPEN;

  @Getter
  @Column(name = "admin_decision", length = 4000)
  private String adminDecision;

  @Getter
  @Column(name = "resolved_at")
  private Instant resolvedAt;

  protected DisputeCase() {}

  public DisputeCase(Deal deal, User openedByUser, String reason, String description) {
    this.deal = deal;
    this.task = deal.getTask();
    this.openedByUser = openedByUser;
    this.reason = reason;
    this.description = description;
  }

  public void markUnderReview() {
    requireStatus(DisputeCaseStatus.OPEN);
    this.status = DisputeCaseStatus.UNDER_REVIEW;
  }

  public void requestCustomerEvidence() {
    requireStatus(
        DisputeCaseStatus.OPEN,
        DisputeCaseStatus.UNDER_REVIEW,
        DisputeCaseStatus.WAITING_FOR_PERFORMER);
    this.status = DisputeCaseStatus.WAITING_FOR_CUSTOMER;
  }

  public void requestPerformerEvidence() {
    requireStatus(
        DisputeCaseStatus.OPEN,
        DisputeCaseStatus.UNDER_REVIEW,
        DisputeCaseStatus.WAITING_FOR_CUSTOMER);
    this.status = DisputeCaseStatus.WAITING_FOR_PERFORMER;
  }

  public boolean acceptsEvidence() {
    return resolvedAt == null
        && switch (status) {
          case OPEN, UNDER_REVIEW, WAITING_FOR_CUSTOMER, WAITING_FOR_PERFORMER -> true;
          default -> false;
        };
  }

  public void resolve(DisputeCaseStatus status, String adminDecision, Instant resolvedAt) {
    if (!acceptsEvidence()) {
      throw new InvalidStatusTransitionException("DisputeCase", this.status.name(), status.name());
    }
    this.status = status;
    this.adminDecision = adminDecision;
    this.resolvedAt = resolvedAt;
  }

  private void requireStatus(DisputeCaseStatus... allowedStatuses) {
    for (DisputeCaseStatus allowedStatus : allowedStatuses) {
      if (status == allowedStatus) {
        return;
      }
    }
    throw new InvalidStatusTransitionException("DisputeCase", status.name(), "REVIEW_TRANSITION");
  }
}
