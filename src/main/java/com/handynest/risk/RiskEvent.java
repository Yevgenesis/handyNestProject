package com.handynest.risk;

import com.handynest.common.domain.PublicIdEntity;
import com.handynest.common.error.InvalidStatusTransitionException;
import com.handynest.identity.User;
import com.handynest.marketplace.ChatMessage;
import com.handynest.marketplace.Deal;
import com.handynest.marketplace.DealChat;
import com.handynest.marketplace.MarketplaceTask;
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
import java.time.Instant;
import lombok.Getter;

@Entity
@Table(name = "risk_event")
public class RiskEvent extends PublicIdEntity {

  @Getter
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Getter
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Getter
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "task_id")
  private MarketplaceTask task;

  @Getter
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "chat_id")
  private DealChat chat;

  @Getter
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "chat_message_id")
  private ChatMessage chatMessage;

  @Getter
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "deal_id")
  private Deal deal;

  @Getter
  @Enumerated(EnumType.STRING)
  @Column(name = "risk_type", nullable = false, length = 40)
  private RiskType riskType;

  @Getter
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private RiskSeverity severity;

  @Getter
  @Column(name = "detected_text", nullable = false, length = 500)
  private String detectedText;

  @Getter
  @Column(name = "normalized_detected_value", nullable = false, length = 500)
  private String normalizedDetectedValue;

  @Getter
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private RiskEventStatus status = RiskEventStatus.OPEN;

  @Getter
  @Column(name = "resolved_at")
  private Instant resolvedAt;

  @Getter
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "resolved_by_admin_id")
  private User resolvedByAdmin;

  @Getter
  @Column(name = "resolution_comment", length = 1000)
  private String resolutionComment;

  protected RiskEvent() {}

  public RiskEvent(ChatMessage message, RiskDetectionResult detection) {
    this.user = message.getSender();
    this.task = message.getChat().getTask();
    this.chat = message.getChat();
    this.chatMessage = message;
    this.riskType = detection.riskType();
    this.severity = detection.severity();
    this.detectedText = detection.detectedText();
    this.normalizedDetectedValue = detection.normalizedDetectedValue();
  }

  public RiskEvent(
      User user,
      MarketplaceTask task,
      RiskType riskType,
      RiskSeverity severity,
      String detectedText,
      String normalizedDetectedValue) {
    this.user = user;
    this.task = task;
    this.riskType = riskType;
    this.severity = severity;
    this.detectedText = detectedText;
    this.normalizedDetectedValue = normalizedDetectedValue;
  }

  public RiskEvent(
      User user,
      Deal deal,
      RiskType riskType,
      RiskSeverity severity,
      String detectedText,
      String normalizedDetectedValue) {
    this.user = user;
    this.deal = deal;
    this.task = deal.getTask();
    this.riskType = riskType;
    this.severity = severity;
    this.detectedText = detectedText;
    this.normalizedDetectedValue = normalizedDetectedValue;
  }

  public void resolve(User admin, String comment, Instant resolvedAt) {
    close(admin, RiskEventStatus.RESOLVED, comment, resolvedAt);
  }

  public void markFalsePositive(User admin, String comment, Instant resolvedAt) {
    close(admin, RiskEventStatus.FALSE_POSITIVE, comment, resolvedAt);
  }

  private void close(User admin, RiskEventStatus nextStatus, String comment, Instant resolvedAt) {
    if (this.status != RiskEventStatus.OPEN) {
      throw new InvalidStatusTransitionException(
          "RiskEvent", this.status.name(), nextStatus.name());
    }
    this.resolvedByAdmin = admin;
    this.status = nextStatus;
    this.resolutionComment = comment;
    this.resolvedAt = resolvedAt;
  }
}
