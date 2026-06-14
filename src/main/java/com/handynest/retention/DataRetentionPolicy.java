package com.handynest.retention;

import com.handynest.common.domain.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "data_retention_policy")
public class DataRetentionPolicy extends BaseAuditEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 80)
  private String name;

  @Column(nullable = false)
  private boolean active;

  @Column(name = "verification_document_retention_days", nullable = false)
  private int verificationDocumentRetentionDays;

  @Column(name = "deleted_user_anonymization_days", nullable = false)
  private int deletedUserAnonymizationDays;

  @Column(name = "chat_retention_days", nullable = false)
  private int chatRetentionDays;

  @Column(name = "audit_log_retention_days", nullable = false)
  private int auditLogRetentionDays;

  @Column(name = "attachment_retention_days", nullable = false)
  private int attachmentRetentionDays;
}
