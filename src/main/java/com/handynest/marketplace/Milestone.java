package com.handynest.marketplace;

import com.handynest.common.domain.PublicIdEntity;
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

@Entity
@Table(name = "milestone")
public class Milestone extends PublicIdEntity {

  @Getter
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Version
  @Column(nullable = false)
  private long version;

  @Getter
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "deal_id", nullable = false)
  private Deal deal;

  @Getter
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "task_id", nullable = false)
  private MarketplaceTask task;

  @Getter
  @Column(nullable = false, length = 160)
  private String title;

  @Getter
  @Column(length = 2000)
  private String description;

  @Getter
  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal amount;

  @Getter
  @Column(nullable = false, length = 3)
  private String currency = "UZS";

  @Getter
  @Column(name = "due_date", nullable = false)
  private Instant dueDate;

  @Getter
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private MilestoneStatus status = MilestoneStatus.PENDING;

  @Getter
  @Column(name = "submitted_at")
  private Instant submittedAt;

  @Getter
  @Column(name = "accepted_at")
  private Instant acceptedAt;

  @Getter
  @Column(name = "rejected_at")
  private Instant rejectedAt;

  protected Milestone() {}

  public Milestone(
      Deal deal,
      String title,
      String description,
      BigDecimal amount,
      String currency,
      Instant dueDate) {
    this.deal = deal;
    this.task = deal.getTask();
    this.title = title;
    this.description = description;
    this.amount = amount;
    this.currency = currency;
    this.dueDate = dueDate;
  }

  public void start() {
    this.status = MilestoneStatus.IN_PROGRESS;
  }

  public void submit(Instant submittedAt) {
    this.status = MilestoneStatus.SUBMITTED;
    this.submittedAt = submittedAt;
  }

  public void accept(Instant acceptedAt) {
    this.status = MilestoneStatus.ACCEPTED;
    this.acceptedAt = acceptedAt;
  }

  public void reject(Instant rejectedAt) {
    this.status = MilestoneStatus.REJECTED;
    this.rejectedAt = rejectedAt;
  }

  public void dispute() {
    this.status = MilestoneStatus.DISPUTED;
  }

  public void cancel() {
    this.status = MilestoneStatus.CANCELED;
  }
}
