package com.handynest.moderation;

import com.handynest.identity.User;
import com.handynest.identity.RoleName;
import com.handynest.identity.UserRepository;
import com.handynest.catalog.category.CategoryQueryRepository;
import com.handynest.common.error.AccessDeniedBusinessException;
import com.handynest.common.error.InvalidStatusTransitionException;
import com.handynest.common.error.ResourceNotFoundException;
import com.handynest.identity.UserProfileService;
import com.handynest.marketplace.ChatMessageRepository;
import com.handynest.marketplace.MarketplaceAttachmentRepository;
import com.handynest.marketplace.MarketplaceFeedbackRepository;
import com.handynest.marketplace.MarketplaceTaskRepository;
import com.handynest.performer.PerformerProfileRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ModerationService {

    private final UserProfileService userProfileService;
    private final UserRepository userRepository;
    private final MarketplaceTaskRepository taskRepository;
    private final PerformerProfileRepository performerProfileRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final CategoryQueryRepository categoryRepository;
    private final MarketplaceFeedbackRepository feedbackRepository;
    private final MarketplaceAttachmentRepository attachmentRepository;
    private final ComplaintRepository complaintRepository;
    private final ModerationCaseRepository moderationCaseRepository;

    @Transactional
    public ComplaintResponse createComplaint(UserDetails userDetails, ComplaintCreateRequest request) {
        User reporter = userProfileService.currentUser(userDetails);
        validateTarget(request.targetType(), request.targetId());
        User targetUser = findTargetUser(request.targetUserId());

        Complaint complaint = new Complaint(
                reporter,
                targetUser,
                request.targetType(),
                request.targetId().trim(),
                request.reason().trim(),
                request.description().trim()
        );
        ModerationCase moderationCase = new ModerationCase(
                request.targetType(),
                request.targetId().trim(),
                reporter,
                request.reason().trim(),
                ModerationPriority.NORMAL
        );
        moderationCase = moderationCaseRepository.save(moderationCase);
        complaint.attachModerationCase(moderationCase);
        complaint = complaintRepository.save(complaint);
        return toComplaintResponse(complaint);
    }

    @Transactional(readOnly = true)
    public List<ComplaintResponse> myComplaints(UserDetails userDetails) {
        User reporter = userProfileService.currentUser(userDetails);
        return complaintRepository.findAllByReporterIdOrderByCreatedAtDesc(reporter.getId()).stream()
                .map(this::toComplaintResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ModerationCaseResponse> adminCases(
            UserDetails userDetails,
            ModerationCaseStatus status,
            ModerationTargetType targetType
    ) {
        User admin = userProfileService.currentUser(userDetails);
        assertAdmin(admin);
        ModerationCaseStatus effectiveStatus = status == null ? ModerationCaseStatus.OPEN : status;
        List<ModerationCase> cases = targetType == null
                ? moderationCaseRepository.findAllByStatusOrderByCreatedAtAsc(effectiveStatus)
                : moderationCaseRepository.findAllByTargetTypeAndStatusOrderByCreatedAtAsc(targetType, effectiveStatus);
        return cases.stream()
                .map(this::toModerationCaseResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ModerationCaseResponse adminCase(UserDetails userDetails, String caseId) {
        User admin = userProfileService.currentUser(userDetails);
        assertAdmin(admin);
        return toModerationCaseResponse(findCase(caseId));
    }

    @Transactional
    public ModerationCaseResponse startReview(UserDetails userDetails, String caseId) {
        User admin = userProfileService.currentUser(userDetails);
        assertAdmin(admin);
        ModerationCase moderationCase = findCase(caseId);
        assertOpen(moderationCase);
        moderationCase.startReview(admin);
        complaintsFor(moderationCase).forEach(Complaint::markInReview);
        return toModerationCaseResponse(moderationCase);
    }

    @Transactional
    public ModerationCaseResponse approve(
            UserDetails userDetails,
            String caseId,
            ModerationDecisionRequest request
    ) {
        return close(userDetails, caseId, ModerationCaseStatus.APPROVED, ModerationDecision.APPROVED, request);
    }

    @Transactional
    public ModerationCaseResponse reject(
            UserDetails userDetails,
            String caseId,
            ModerationDecisionRequest request
    ) {
        return close(userDetails, caseId, ModerationCaseStatus.REJECTED, ModerationDecision.REJECTED, request);
    }

    @Transactional
    public ModerationCaseResponse resolve(
            UserDetails userDetails,
            String caseId,
            ModerationDecisionRequest request
    ) {
        return close(userDetails, caseId, ModerationCaseStatus.RESOLVED, ModerationDecision.RESOLVED, request);
    }

    private ModerationCaseResponse close(
            UserDetails userDetails,
            String caseId,
            ModerationCaseStatus status,
            ModerationDecision decision,
            ModerationDecisionRequest request
    ) {
        User admin = userProfileService.currentUser(userDetails);
        assertAdmin(admin);
        ModerationCase moderationCase = findCase(caseId);
        assertCanClose(moderationCase);
        if (moderationCase.getAssignedAdmin() == null) {
            moderationCase.assignTo(admin);
        }
        Instant resolvedAt = Instant.now();
        moderationCase.close(status, decision, request == null ? null : blankToNull(request.comment()), resolvedAt);
        complaintsFor(moderationCase).forEach(complaint -> complaint.markResolved(resolvedAt));
        return toModerationCaseResponse(moderationCase);
    }

    private void validateTarget(ModerationTargetType targetType, String targetId) {
        String normalizedTargetId = targetId.trim();
        switch (targetType) {
            case TASK -> taskRepository.findByPublicId(normalizedTargetId)
                    .orElseThrow(() -> new ResourceNotFoundException("Task", normalizedTargetId));
            case PROFILE -> performerProfileRepository.findByPublicId(normalizedTargetId)
                    .orElseThrow(() -> new ResourceNotFoundException("PerformerProfile", normalizedTargetId));
            case CHAT_MESSAGE -> chatMessageRepository.findByPublicIdAndDeletedAtIsNull(normalizedTargetId)
                    .orElseThrow(() -> new ResourceNotFoundException("ChatMessage", normalizedTargetId));
            case CATEGORY -> categoryRepository.findByPublicId(normalizedTargetId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category", normalizedTargetId));
            case FEEDBACK -> feedbackRepository.findByPublicId(normalizedTargetId)
                    .orElseThrow(() -> new ResourceNotFoundException("Feedback", normalizedTargetId));
            case ATTACHMENT -> attachmentRepository.findByPublicIdAndDeletedAtIsNull(normalizedTargetId)
                    .orElseThrow(() -> new ResourceNotFoundException("Attachment", normalizedTargetId));
        }
    }

    private User findTargetUser(String targetUserId) {
        String normalizedTargetUserId = blankToNull(targetUserId);
        if (normalizedTargetUserId == null) {
            return null;
        }
        return userRepository.findByPublicId(normalizedTargetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", normalizedTargetUserId));
    }

    private ModerationCase findCase(String caseId) {
        return moderationCaseRepository.findByPublicId(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("ModerationCase", caseId));
    }

    private List<Complaint> complaintsFor(ModerationCase moderationCase) {
        return complaintRepository.findAllByModerationCaseId(moderationCase.getId());
    }

    private void assertAdmin(User user) {
        if (!user.getRoles().contains(RoleName.ADMIN)) {
            throw new AccessDeniedBusinessException("Admin role is required");
        }
    }

    private void assertOpen(ModerationCase moderationCase) {
        if (moderationCase.getStatus() != ModerationCaseStatus.OPEN) {
            throw new InvalidStatusTransitionException(
                    "ModerationCase",
                    moderationCase.getStatus().name(),
                    ModerationCaseStatus.IN_REVIEW.name()
            );
        }
    }

    private void assertCanClose(ModerationCase moderationCase) {
        if (moderationCase.getResolvedAt() != null) {
            throw new InvalidStatusTransitionException(
                    "ModerationCase",
                    moderationCase.getStatus().name(),
                    "FINAL"
            );
        }
    }

    private ComplaintResponse toComplaintResponse(Complaint complaint) {
        return new ComplaintResponse(
                complaint.getPublicId(),
                complaint.getReporter().getPublicId(),
                complaint.getTargetUser() == null ? null : complaint.getTargetUser().getPublicId(),
                complaint.getTargetType().name(),
                complaint.getTargetId(),
                complaint.getReason(),
                complaint.getDescription(),
                complaint.getStatus().name(),
                complaint.getModerationCase() == null ? null : complaint.getModerationCase().getPublicId(),
                complaint.getCreatedAt(),
                complaint.getResolvedAt()
        );
    }

    private ModerationCaseResponse toModerationCaseResponse(ModerationCase moderationCase) {
        return new ModerationCaseResponse(
                moderationCase.getPublicId(),
                moderationCase.getTargetType().name(),
                moderationCase.getTargetId(),
                moderationCase.getOpenedByUser() == null ? null : moderationCase.getOpenedByUser().getPublicId(),
                moderationCase.getAssignedAdmin() == null ? null : moderationCase.getAssignedAdmin().getPublicId(),
                moderationCase.getReason(),
                moderationCase.getStatus().name(),
                moderationCase.getPriority().name(),
                moderationCase.getDecision() == null ? null : moderationCase.getDecision().name(),
                moderationCase.getDecisionComment(),
                moderationCase.getCreatedAt(),
                moderationCase.getUpdatedAt(),
                moderationCase.getResolvedAt()
        );
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
