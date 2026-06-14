package com.handynest.common.audit;

import com.handynest.common.domain.PublicIdEntity;
import com.handynest.identity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "audit_log")
public class AuditLog extends PublicIdEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "actor_user_id")
  private User actor;

  @Column(nullable = false, length = 80)
  private String action;

  @Column(name = "entity_type", nullable = false, length = 80)
  private String entityType;

  @Column(name = "entity_public_id", nullable = false, length = 80)
  private String entityPublicId;

  @Column(name = "metadata_json", nullable = false, columnDefinition = "TEXT")
  private String metadataJson;

  @Column(name = "ip_address", length = 64)
  private String ipAddress;

  @Column(name = "user_agent", length = 500)
  private String userAgent;

  protected AuditLog() {}

  public AuditLog(
      User actor,
      String action,
      String entityType,
      String entityPublicId,
      String metadataJson,
      String ipAddress,
      String userAgent) {
    this.actor = actor;
    this.action = action;
    this.entityType = entityType;
    this.entityPublicId = entityPublicId;
    this.metadataJson = metadataJson;
    this.ipAddress = ipAddress;
    this.userAgent = userAgent;
  }
}
