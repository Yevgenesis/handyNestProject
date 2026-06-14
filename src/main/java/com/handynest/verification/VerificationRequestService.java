package com.handynest.verification;

import com.handynest.common.error.AccessDeniedBusinessException;
import com.handynest.common.error.BadRequestBusinessException;
import com.handynest.common.error.DuplicateResourceException;
import com.handynest.common.error.InvalidStatusTransitionException;
import com.handynest.common.error.ResourceNotFoundException;
import com.handynest.files.FileStorageService;
import com.handynest.files.PresignedStorageUrl;
import com.handynest.identity.RoleName;
import com.handynest.identity.User;
import com.handynest.identity.UserProfileService;
import com.handynest.marketplace.AttachmentDownloadUrlResponse;
import com.handynest.marketplace.AttachmentResponse;
import com.handynest.marketplace.AttachmentType;
import com.handynest.marketplace.AttachmentVisibility;
import com.handynest.marketplace.MarketplaceAttachment;
import com.handynest.marketplace.MarketplaceAttachmentRepository;
import com.handynest.notification.NotificationService;
import com.handynest.notification.NotificationType;
import com.handynest.performer.PerformerProfile;
import com.handynest.performer.PerformerProfileRepository;
import com.handynest.performer.PerformerVerificationLevel;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VerificationRequestService {

  private final UserProfileService userProfileService;
  private final PerformerProfileRepository performerProfileRepository;
  private final MarketplaceAttachmentRepository attachmentRepository;
  private final VerificationRequestRepository verificationRequestRepository;
  private final VerificationAuditEventRepository verificationAuditEventRepository;
  private final NotificationService notificationService;
  private final FileStorageService fileStorageService;

  @Transactional
  public VerificationRequestResponse submit(
      UserDetails userDetails, VerificationRequestCreateRequest request) {
    User user = userProfileService.currentUser(userDetails);
    PerformerProfile performerProfile =
        performerProfileRepository
            .findByUserId(user.getId())
            .orElseThrow(
                () -> new ResourceNotFoundException("PerformerProfile", user.getPublicId()));
    List<MarketplaceAttachment> documents = validateSubmitRequest(user, performerProfile, request);

    VerificationRequest verificationRequest =
        verificationRequestRepository.save(
            new VerificationRequest(
                performerProfile, user, request.requestedLevel(), blankToNull(request.comment())));

    for (MarketplaceAttachment document : documents) {
      verificationRequest.addDocument(document);
    }

    performerProfile.markVerificationPending();
    recordAudit(verificationRequest, user, null, VerificationAuditAction.SUBMITTED);
    return toResponse(verificationRequest);
  }

  @Transactional(readOnly = true)
  public List<VerificationRequestResponse> myRequests(UserDetails userDetails) {
    User user = userProfileService.currentUser(userDetails);
    return verificationRequestRepository
        .findAllByRequestedByIdOrderByCreatedAtDesc(user.getId())
        .stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<VerificationRequestResponse> adminList(
      UserDetails userDetails, VerificationRequestStatus status) {
    User admin = userProfileService.currentUser(userDetails);
    assertSupport(admin);
    VerificationRequestStatus effectiveStatus =
        status == null ? VerificationRequestStatus.PENDING : status;
    return verificationRequestRepository
        .findAllByStatusOrderByCreatedAtAsc(effectiveStatus)
        .stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional
  public VerificationRequestResponse approve(
      UserDetails userDetails, String requestId, VerificationDecisionRequest decision) {
    User admin = userProfileService.currentUser(userDetails);
    assertAdmin(admin);
    VerificationRequest verificationRequest = findRequestForUpdate(requestId);
    assertPending(verificationRequest);
    Instant reviewedAt = Instant.now();
    verificationRequest.approve(admin, reviewedAt);
    verificationRequest
        .getPerformerProfile()
        .approveVerification(verificationRequest.getRequestedLevel(), reviewedAt);
    recordAudit(verificationRequest, admin, null, VerificationAuditAction.APPROVED);
    notificationService.notifyUser(
        verificationRequest.getRequestedBy(),
        NotificationType.VERIFICATION_APPROVED,
        "Верификация одобрена",
        "Ваш запрос на верификацию одобрен",
        "VerificationRequest",
        verificationRequest.getPublicId(),
        java.util.Map.of("level", verificationRequest.getRequestedLevel().name()));
    return toResponse(verificationRequest);
  }

  @Transactional
  public VerificationRequestResponse reject(
      UserDetails userDetails, String requestId, VerificationDecisionRequest decision) {
    User admin = userProfileService.currentUser(userDetails);
    assertAdmin(admin);
    String reason = decision == null ? null : blankToNull(decision.reason());
    if (reason == null) {
      throw new BadRequestBusinessException("rejection reason is required");
    }
    VerificationRequest verificationRequest = findRequestForUpdate(requestId);
    assertPending(verificationRequest);
    Instant reviewedAt = Instant.now();
    verificationRequest.reject(admin, reason, reviewedAt);
    verificationRequest.getPerformerProfile().rejectVerification(reason, reviewedAt);
    recordAudit(verificationRequest, admin, null, VerificationAuditAction.REJECTED);
    notificationService.notifyUser(
        verificationRequest.getRequestedBy(),
        NotificationType.VERIFICATION_REJECTED,
        "Верификация отклонена",
        reason,
        "VerificationRequest",
        verificationRequest.getPublicId(),
        java.util.Map.of("level", verificationRequest.getRequestedLevel().name()));
    return toResponse(verificationRequest);
  }

  @Transactional
  public AttachmentDownloadUrlResponse adminDocumentDownloadUrl(
      UserDetails userDetails, String requestId, String documentId) {
    User admin = userProfileService.currentUser(userDetails);
    assertSupport(admin);
    VerificationRequest verificationRequest = findRequest(requestId);
    MarketplaceAttachment document =
        verificationRequest.getDocuments().stream()
            .filter(candidate -> candidate.getPublicId().equals(documentId))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("VerificationDocument", documentId));
    assertVerificationDocument(document);

    PresignedStorageUrl downloadUrl = fileStorageService.createDownloadUrl(document);
    recordAudit(
        verificationRequest, admin, document, VerificationAuditAction.DOCUMENT_DOWNLOAD_URL_ISSUED);
    return new AttachmentDownloadUrlResponse(
        document.getPublicId(),
        downloadUrl.url(),
        downloadUrl.method(),
        downloadUrl.headers(),
        downloadUrl.expiresAt());
  }

  private List<MarketplaceAttachment> validateSubmitRequest(
      User user, PerformerProfile performerProfile, VerificationRequestCreateRequest request) {
    assertRequestedLevel(user, performerProfile, request.requestedLevel());
    if (verificationRequestRepository.existsByPerformerProfileIdAndStatus(
        performerProfile.getId(), VerificationRequestStatus.PENDING)) {
      throw new DuplicateResourceException("Performer already has a pending verification request");
    }
    Set<String> documentIds = uniqueDocumentIds(request.documentIds());
    if (documentIds.isEmpty()) {
      throw new BadRequestBusinessException("At least one verification document is required");
    }
    List<MarketplaceAttachment> documents =
        documentIds.stream()
            .map(this::findAttachment)
            .peek(document -> assertUsableVerificationDocument(document, user))
            .toList();
    Set<VerificationDocumentType> documentTypes =
        documents.stream()
            .map(MarketplaceAttachment::getVerificationDocumentType)
            .collect(java.util.stream.Collectors.toSet());
    if (!documentTypes.contains(VerificationDocumentType.IDENTITY_DOCUMENT)
        || !documentTypes.contains(VerificationDocumentType.SELFIE)) {
      throw new BadRequestBusinessException(
          "ID verification requires IDENTITY_DOCUMENT and SELFIE");
    }
    return documents;
  }

  private void assertRequestedLevel(
      User user, PerformerProfile performerProfile, PerformerVerificationLevel requestedLevel) {
    if (requestedLevel == PerformerVerificationLevel.PHONE_VERIFIED) {
      throw new BadRequestBusinessException("Phone verification must be completed through OTP");
    }
    if (requestedLevel != PerformerVerificationLevel.ID_VERIFIED) {
      throw new BadRequestBusinessException("Requested verification level is not available in MVP");
    }
    if (!user.isPhoneVerified()
        || performerProfile.getEffectiveVerificationLevel().ordinal()
            < PerformerVerificationLevel.PHONE_VERIFIED.ordinal()) {
      throw new com.handynest.common.error.VerificationRequiredException(
          "Verified phone is required before ID verification");
    }
    if (performerProfile.getEffectiveVerificationLevel().ordinal() >= requestedLevel.ordinal()) {
      throw new DuplicateResourceException("Requested verification level is already granted");
    }
  }

  private Set<String> uniqueDocumentIds(List<String> documentIds) {
    Set<String> uniqueIds = new LinkedHashSet<>();
    for (String documentId : documentIds) {
      String normalizedDocumentId = blankToNull(documentId);
      if (normalizedDocumentId == null) {
        throw new BadRequestBusinessException("documentIds must not contain blank values");
      }
      if (!uniqueIds.add(normalizedDocumentId)) {
        throw new BadRequestBusinessException(
            "Duplicate verification document: " + normalizedDocumentId);
      }
    }
    return uniqueIds;
  }

  private MarketplaceAttachment findAttachment(String attachmentId) {
    return attachmentRepository
        .findByPublicIdAndDeletedAtIsNull(attachmentId)
        .orElseThrow(() -> new ResourceNotFoundException("Attachment", attachmentId));
  }

  private VerificationRequest findRequest(String requestId) {
    return verificationRequestRepository
        .findByPublicId(requestId)
        .orElseThrow(() -> new ResourceNotFoundException("VerificationRequest", requestId));
  }

  private VerificationRequest findRequestForUpdate(String requestId) {
    return verificationRequestRepository
        .findByPublicIdForUpdate(requestId)
        .orElseThrow(() -> new ResourceNotFoundException("VerificationRequest", requestId));
  }

  private void assertUsableVerificationDocument(MarketplaceAttachment document, User owner) {
    if (!document.getOwner().getId().equals(owner.getId())) {
      throw new AccessDeniedBusinessException("Only document owner can submit it for verification");
    }
    if (document.getAttachmentType() != AttachmentType.VERIFICATION_DOCUMENT
        || document.getVisibility() != AttachmentVisibility.ADMIN_ONLY) {
      throw new BadRequestBusinessException("Only private verification documents can be submitted");
    }
    if (document.getVerificationRequest() != null) {
      throw new DuplicateResourceException(
          "Verification document is already attached to a request");
    }
    if (document.getVerificationDocumentType() == null) {
      throw new BadRequestBusinessException("Verification document type is required");
    }
  }

  private void assertVerificationDocument(MarketplaceAttachment document) {
    if (document.getAttachmentType() != AttachmentType.VERIFICATION_DOCUMENT
        || document.getVisibility() != AttachmentVisibility.ADMIN_ONLY) {
      throw new BadRequestBusinessException("Invalid verification document");
    }
  }

  private void assertAdmin(User user) {
    if (!user.getRoles().contains(RoleName.ADMIN)) {
      throw new AccessDeniedBusinessException("Admin role is required");
    }
  }

  private void assertSupport(User user) {
    if (!user.getRoles().contains(RoleName.ADMIN)
        && !user.getRoles().contains(RoleName.MODERATOR)) {
      throw new AccessDeniedBusinessException("Support role is required");
    }
  }

  private void assertPending(VerificationRequest verificationRequest) {
    if (verificationRequest.getStatus() != VerificationRequestStatus.PENDING) {
      throw new InvalidStatusTransitionException(
          "VerificationRequest", verificationRequest.getStatus().name(), "REVIEWED");
    }
  }

  private VerificationRequestResponse toResponse(VerificationRequest verificationRequest) {
    return new VerificationRequestResponse(
        verificationRequest.getPublicId(),
        verificationRequest.getPerformerProfile().getPublicId(),
        verificationRequest.getRequestedBy().getPublicId(),
        verificationRequest.getRequestedLevel().name(),
        verificationRequest.getStatus().name(),
        verificationRequest.getComment(),
        verificationRequest.getReviewedBy() == null
            ? null
            : verificationRequest.getReviewedBy().getPublicId(),
        verificationRequest.getReviewedAt(),
        verificationRequest.getRejectionReason(),
        verificationRequest.getDocuments().stream().map(this::toAttachmentResponse).toList(),
        verificationRequest.getCreatedAt(),
        verificationRequest.getUpdatedAt());
  }

  private AttachmentResponse toAttachmentResponse(MarketplaceAttachment attachment) {
    return new AttachmentResponse(
        attachment.getPublicId(),
        attachment.getOwner().getPublicId(),
        attachment.getTask() == null ? null : attachment.getTask().getPublicId(),
        attachment.getChatMessage() == null ? null : attachment.getChatMessage().getPublicId(),
        attachment.getDisputeCase() == null ? null : attachment.getDisputeCase().getPublicId(),
        attachment.getAttachmentType().name(),
        attachment.getOriginalFilename(),
        attachment.getContentType(),
        attachment.getSizeBytes(),
        attachment.getVerificationDocumentType() == null
            ? null
            : attachment.getVerificationDocumentType().name(),
        attachment.getCreatedAt());
  }

  private String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value.trim();
  }

  private void recordAudit(
      VerificationRequest verificationRequest,
      User actor,
      MarketplaceAttachment attachment,
      VerificationAuditAction action) {
    verificationAuditEventRepository.save(
        new VerificationAuditEvent(verificationRequest, actor, attachment, action));
  }
}
