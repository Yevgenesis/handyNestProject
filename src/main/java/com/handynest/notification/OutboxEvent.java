package com.handynest.notification;

import com.handynest.common.domain.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;

@Entity
@Table(name = "outbox_event")
public class OutboxEvent extends BaseAuditEntity {

  @Getter
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Getter
  @Column(name = "event_type", nullable = false, length = 120)
  private String eventType;

  @Getter
  @Column(name = "aggregate_type", nullable = false, length = 80)
  private String aggregateType;

  @Getter
  @Column(name = "aggregate_id", nullable = false, length = 80)
  private String aggregateId;

  @Getter
  @Column(name = "payload_json", nullable = false, columnDefinition = "TEXT")
  private String payloadJson;

  @Getter
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private OutboxStatus status = OutboxStatus.PENDING;

  @Getter
  @Column(nullable = false)
  private int attempts;

  @Getter
  @Column(name = "next_attempt_at", nullable = false)
  private Instant nextAttemptAt;

  @Getter
  @Column(name = "processed_at")
  private Instant processedAt;

  @Getter
  @Column(name = "last_error", length = 1000)
  private String lastError;

  protected OutboxEvent() {}

  public OutboxEvent(
      String eventType, String aggregateType, String aggregateId, String payloadJson, Instant now) {
    this.eventType = eventType;
    this.aggregateType = aggregateType;
    this.aggregateId = aggregateId;
    this.payloadJson = payloadJson;
    this.nextAttemptAt = now;
  }

  public void markProcessing() {
    this.status = OutboxStatus.PROCESSING;
    this.attempts++;
    this.lastError = null;
  }

  public void markProcessed(Instant processedAt) {
    this.status = OutboxStatus.PROCESSED;
    this.processedAt = processedAt;
    this.lastError = null;
  }

  public void markFailed(String lastError, Instant nextAttemptAt, boolean deadLetter) {
    this.status = deadLetter ? OutboxStatus.DEAD_LETTER : OutboxStatus.FAILED;
    this.lastError = lastError;
    this.nextAttemptAt = nextAttemptAt;
  }

  public void retryAt(Instant nextAttemptAt) {
    this.status = OutboxStatus.PENDING;
    this.nextAttemptAt = nextAttemptAt;
  }
}
