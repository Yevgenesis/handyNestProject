package com.handynest.notification;

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
import java.time.Instant;
import lombok.Getter;

@Entity
@Table(name = "notification")
public class Notification extends PublicIdEntity {

  @Getter
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Getter
  @Column(name = "source_event_id", nullable = false)
  private Long sourceEventId;

  @Getter
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Getter
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 40)
  private NotificationType type;

  @Getter
  @Column(nullable = false, length = 160)
  private String title;

  @Getter
  @Column(nullable = false, length = 1000)
  private String body;

  @Getter
  @Column(name = "target_type", nullable = false, length = 80)
  private String targetType;

  @Getter
  @Column(name = "target_id", nullable = false, length = 80)
  private String targetId;

  @Getter
  @Column(name = "read_at")
  private Instant readAt;

  @Getter
  @Column(name = "delivered_at")
  private Instant deliveredAt;

  @Getter
  @Column(name = "metadata_json", columnDefinition = "TEXT")
  private String metadataJson;

  protected Notification() {}

  public Notification(
      Long sourceEventId,
      User user,
      NotificationType type,
      String title,
      String body,
      String targetType,
      String targetId,
      String metadataJson) {
    this.sourceEventId = sourceEventId;
    this.user = user;
    this.type = type;
    this.title = title;
    this.body = body;
    this.targetType = targetType;
    this.targetId = targetId;
    this.metadataJson = metadataJson;
  }

  public void markRead(Instant readAt) {
    if (this.readAt == null) {
      this.readAt = readAt;
    }
  }

  public void markDelivered(Instant deliveredAt) {
    if (this.deliveredAt == null) {
      this.deliveredAt = deliveredAt;
    }
  }
}
