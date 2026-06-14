package com.handynest.marketplace;

import com.handynest.common.domain.PublicIdEntity;
import com.handynest.identity.User;
import com.handynest.verification.VerificationDocumentType;
import com.handynest.verification.VerificationRequest;
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
@Table(name = "marketplace_attachment")
public class MarketplaceAttachment extends PublicIdEntity {

  @Getter
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Version
  @Column(nullable = false)
  private long version;

  @Getter
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "owner_id", nullable = false)
  private User owner;

  @Getter
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "task_id")
  private MarketplaceTask task;

  @Getter
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "chat_message_id")
  private ChatMessage chatMessage;

  @Getter
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "dispute_case_id")
  private DisputeCase disputeCase;

  @Getter
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "verification_request_id")
  private VerificationRequest verificationRequest;

  @Getter
  @Enumerated(EnumType.STRING)
  @Column(name = "attachment_type", nullable = false, length = 32)
  private AttachmentType attachmentType;

  @Getter
  @Enumerated(EnumType.STRING)
  @Column(name = "storage_provider", nullable = false, length = 32)
  private StorageProvider storageProvider;

  @Getter
  @Column(nullable = false, length = 80)
  private String bucket;

  @Getter
  @Column(name = "storage_key", nullable = false, length = 500)
  private String storageKey;

  @Getter
  @Column(name = "original_filename", nullable = false, length = 255)
  private String originalFilename;

  @Getter
  @Column(name = "content_type", nullable = false, length = 120)
  private String contentType;

  @Getter
  @Column(name = "size_bytes", nullable = false)
  private long sizeBytes;

  @Getter
  @Column(length = 128)
  private String checksum;

  @Getter
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private AttachmentVisibility visibility;

  @Getter
  @Enumerated(EnumType.STRING)
  @Column(name = "verification_document_type", length = 40)
  private VerificationDocumentType verificationDocumentType;

  @Getter
  @Column(name = "deleted_at")
  private Instant deletedAt;

  @Getter
  @Column(name = "storage_deleted_at")
  private Instant storageDeletedAt;

  @Getter
  @Column(name = "storage_deletion_attempts", nullable = false)
  private int storageDeleteAttempts;

  @Getter
  @Column(name = "storage_deletion_last_error", length = 1000)
  private String storageDeleteLastError;

  protected MarketplaceAttachment() {}

  public MarketplaceAttachment(
      String publicId,
      User owner,
      MarketplaceTask task,
      DisputeCase disputeCase,
      AttachmentType attachmentType,
      StorageProvider storageProvider,
      String bucket,
      String storageKey,
      String originalFilename,
      String contentType,
      long sizeBytes,
      String checksum,
      AttachmentVisibility visibility) {
    setPublicId(publicId);
    this.owner = owner;
    this.task = task;
    this.disputeCase = disputeCase;
    this.attachmentType = attachmentType;
    this.storageProvider = storageProvider;
    this.bucket = bucket;
    this.storageKey = storageKey;
    this.originalFilename = originalFilename;
    this.contentType = contentType;
    this.sizeBytes = sizeBytes;
    this.checksum = checksum;
    this.visibility = visibility;
  }

  public void attachToMessage(ChatMessage chatMessage) {
    this.chatMessage = chatMessage;
  }

  public void attachToVerificationRequest(VerificationRequest verificationRequest) {
    this.verificationRequest = verificationRequest;
  }

  public void classifyVerificationDocument(VerificationDocumentType documentType) {
    if (attachmentType != AttachmentType.VERIFICATION_DOCUMENT) {
      throw new IllegalStateException("Only verification documents can be classified");
    }
    this.verificationDocumentType = documentType;
  }

  public void markDeleted(Instant deletedAt) {
    if (this.deletedAt == null) {
      this.deletedAt = deletedAt;
    }
  }

  public void markStorageDeletionSucceeded(Instant deletedAt) {
    storageDeleteAttempts++;
    storageDeletedAt = deletedAt;
    storageDeleteLastError = null;
  }

  public void markStorageDeletionFailed(String error) {
    storageDeleteAttempts++;
    storageDeleteLastError = error;
  }
}
