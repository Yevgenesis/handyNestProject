package com.handynest.marketplace;

import com.handynest.common.domain.PublicIdEntity;
import com.handynest.identity.User;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;

@Entity
@Table(name = "deal_chat")
public class DealChat extends PublicIdEntity {

  @Getter
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

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
  @JoinColumn(name = "customer_id", nullable = false)
  private User customer;

  @Getter
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "performer_id", nullable = false)
  private PerformerProfile performer;

  @Getter
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ChatStatus status = ChatStatus.ACTIVE;

  @Getter
  @Column(name = "closed_at")
  private Instant closedAt;

  protected DealChat() {}

  public DealChat(Deal deal) {
    this.deal = deal;
    this.task = deal.getTask();
    this.customer = deal.getCustomer();
    this.performer = deal.getPerformer();
  }

  public void makeReadOnly(Instant closedAt) {
    this.status = ChatStatus.READ_ONLY;
    this.closedAt = closedAt;
  }
}
