package com.handynest.verification;

import com.handynest.common.domain.PublicIdEntity;
import com.handynest.identity.User;
import com.handynest.marketplace.MarketplaceAttachment;
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

@Entity
@Table(name = "verification_audit_event")
public class VerificationAuditEvent extends PublicIdEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "verification_request_id", nullable = false)
  private VerificationRequest verificationRequest;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "actor_user_id", nullable = false)
  private User actor;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "attachment_id")
  private MarketplaceAttachment attachment;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 48)
  private VerificationAuditAction action;

  protected VerificationAuditEvent() {}

  public VerificationAuditEvent(
      VerificationRequest verificationRequest,
      User actor,
      MarketplaceAttachment attachment,
      VerificationAuditAction action) {
    this.verificationRequest = verificationRequest;
    this.actor = actor;
    this.attachment = attachment;
    this.action = action;
  }
}
