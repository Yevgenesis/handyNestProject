package com.handynest.verification;

import com.handynest.identity.User;
import com.handynest.identity.RoleName;
import com.handynest.common.error.AccessDeniedBusinessException;
import com.handynest.common.error.BadRequestBusinessException;
import com.handynest.common.error.DuplicateResourceException;
import com.handynest.common.error.InvalidStatusTransitionException;
import com.handynest.common.error.ResourceNotFoundException;
import com.handynest.identity.UserProfileService;
import com.handynest.marketplace.AttachmentResponse;
import com.handynest.marketplace.AttachmentType;
import com.handynest.marketplace.AttachmentVisibility;
import com.handynest.marketplace.MarketplaceAttachment;
import com.handynest.marketplace.MarketplaceAttachmentRepository;
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

    @Transactional
    public VerificationRequestResponse submit(
            UserDetails userDetails,
            VerificationRequestCreateRequest request
    ) {
        User user = userProfileService.currentUser(userDetails);
        PerformerProfile performerProfile = performerProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("PerformerProfile", user.getPublicId()));
        Set<String> documentIds = validateSubmitRequest(performerProfile, request);

        VerificationRequest verificationRequest = verificationRequestRepository.save(new VerificationRequest(
                performerProfile,
                user,
                request.requestedLevel(),
                blankToNull(request.comment())
        ));

        for (String documentId : documentIds) {
            MarketplaceAttachment document = findAttachment(documentId);
            assertUsableVerificationDocument(document, user);
            verificationRequest.addDocument(document);
        }

        performerProfile.markVerificationPending();
        return toResponse(verificationRequest);
    }

    @Transactional(readOnly = true)
    public List<VerificationRequestResponse> myRequests(UserDetails userDetails) {
        User user = userProfileService.currentUser(userDetails);
        return verificationRequestRepository.findAllByRequestedByIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VerificationRequestResponse> adminList(
            UserDetails userDetails,
            VerificationRequestStatus status
    ) {
        User admin = userProfileService.currentUser(userDetails);
        assertAdmin(admin);
        VerificationRequestStatus effectiveStatus = status == null ? VerificationRequestStatus.PENDING : status;
        return verificationRequestRepository.findAllByStatusOrderByCreatedAtAsc(effectiveStatus).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public VerificationRequestResponse approve(
            UserDetails userDetails,
            String requestId,
            VerificationDecisionRequest decision
    ) {
        User admin = userProfileService.currentUser(userDetails);
        assertAdmin(admin);
        VerificationRequest verificationRequest = findRequest(requestId);
        assertPending(verificationRequest);
        Instant reviewedAt = Instant.now();
        verificationRequest.approve(admin, reviewedAt);
        verificationRequest.getPerformerProfile().approveVerification(
                verificationRequest.getRequestedLevel(),
                reviewedAt
        );
        return toResponse(verificationRequest);
    }

    @Transactional
    public VerificationRequestResponse reject(
            UserDetails userDetails,
            String requestId,
            VerificationDecisionRequest decision
    ) {
        User admin = userProfileService.currentUser(userDetails);
        assertAdmin(admin);
        String reason = decision == null ? null : blankToNull(decision.reason());
        if (reason == null) {
            throw new BadRequestBusinessException("rejection reason is required");
        }
        VerificationRequest verificationRequest = findRequest(requestId);
        assertPending(verificationRequest);
        Instant reviewedAt = Instant.now();
        verificationRequest.reject(admin, reason, reviewedAt);
        verificationRequest.getPerformerProfile().rejectVerification(reason, reviewedAt);
        return toResponse(verificationRequest);
    }

    private Set<String> validateSubmitRequest(
            PerformerProfile performerProfile,
            VerificationRequestCreateRequest request
    ) {
        if (request.requestedLevel() == PerformerVerificationLevel.NONE) {
            throw new BadRequestBusinessException("requestedLevel must be higher than NONE");
        }
        if (verificationRequestRepository.existsByPerformerProfileIdAndStatus(
                performerProfile.getId(),
                VerificationRequestStatus.PENDING
        )) {
            throw new DuplicateResourceException("Performer already has a pending verification request");
        }
        Set<String> documentIds = uniqueDocumentIds(request.documentIds());
        if (documentIds.isEmpty()) {
            throw new BadRequestBusinessException("At least one verification document is required");
        }
        return documentIds;
    }

    private Set<String> uniqueDocumentIds(List<String> documentIds) {
        Set<String> uniqueIds = new LinkedHashSet<>();
        for (String documentId : documentIds) {
            String normalizedDocumentId = blankToNull(documentId);
            if (normalizedDocumentId == null) {
                throw new BadRequestBusinessException("documentIds must not contain blank values");
            }
            if (!uniqueIds.add(normalizedDocumentId)) {
                throw new BadRequestBusinessException("Duplicate verification document: " + normalizedDocumentId);
            }
        }
        return uniqueIds;
    }

    private MarketplaceAttachment findAttachment(String attachmentId) {
        return attachmentRepository.findByPublicIdAndDeletedAtIsNull(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment", attachmentId));
    }

    private VerificationRequest findRequest(String requestId) {
        return verificationRequestRepository.findByPublicId(requestId)
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
            throw new DuplicateResourceException("Verification document is already attached to a request");
        }
    }

    private void assertAdmin(User user) {
        if (!user.getRoles().contains(RoleName.ADMIN)) {
            throw new AccessDeniedBusinessException("Admin role is required");
        }
    }

    private void assertPending(VerificationRequest verificationRequest) {
        if (verificationRequest.getStatus() != VerificationRequestStatus.PENDING) {
            throw new InvalidStatusTransitionException(
                    "VerificationRequest",
                    verificationRequest.getStatus().name(),
                    "REVIEWED"
            );
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
                verificationRequest.getReviewedBy() == null ? null : verificationRequest.getReviewedBy().getPublicId(),
                verificationRequest.getReviewedAt(),
                verificationRequest.getRejectionReason(),
                verificationRequest.getDocuments().stream()
                        .map(this::toAttachmentResponse)
                        .toList(),
                verificationRequest.getCreatedAt(),
                verificationRequest.getUpdatedAt()
        );
    }

    private AttachmentResponse toAttachmentResponse(MarketplaceAttachment attachment) {
        return new AttachmentResponse(
                attachment.getPublicId(),
                attachment.getOwner().getPublicId(),
                attachment.getTask() == null ? null : attachment.getTask().getPublicId(),
                attachment.getChatMessage() == null ? null : attachment.getChatMessage().getPublicId(),
                attachment.getDisputeCase() == null ? null : attachment.getDisputeCase().getPublicId(),
                attachment.getAttachmentType().name(),
                attachment.getStorageProvider().name(),
                attachment.getBucket(),
                attachment.getStorageKey(),
                attachment.getOriginalFilename(),
                attachment.getContentType(),
                attachment.getSizeBytes(),
                attachment.getChecksum(),
                attachment.getVisibility().name(),
                attachment.getCreatedAt()
        );
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
