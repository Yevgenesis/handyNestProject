package com.handynest.marketplace;

import com.handynest.common.domain.PublicIdEntity;
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
import jakarta.persistence.Version;
import lombok.Getter;

@Entity
@Table(
    name = "marketplace_feedback",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_marketplace_feedback_deal_sender",
            columnNames = {"deal_id", "sender_id"}))
public class MarketplaceFeedback extends PublicIdEntity {

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
  @JoinColumn(name = "deal_id", nullable = false)
  private Deal deal;

  @Getter
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "sender_id", nullable = false)
  private User sender;

  @Getter
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "receiver_id", nullable = false)
  private User receiver;

  @Getter
  @Column(nullable = false)
  private int grade;

  @Getter
  @Column(length = 2000)
  private String text;

  @Getter
  @Column(name = "hidden_by_admin", nullable = false)
  private boolean hiddenByAdmin;

  @Getter
  @Enumerated(EnumType.STRING)
  @Column(name = "moderation_status", nullable = false, length = 32)
  private FeedbackModerationStatus moderationStatus = FeedbackModerationStatus.VISIBLE;

  protected MarketplaceFeedback() {}

  public MarketplaceFeedback(Deal deal, User sender, User receiver, int grade, String text) {
    this.task = deal.getTask();
    this.deal = deal;
    this.sender = sender;
    this.receiver = receiver;
    this.grade = grade;
    this.text = text;
  }
}
