package com.handynest.moderation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.handynest.catalog.category.CategoryQueryRepository;
import com.handynest.common.api.PageResponse;
import com.handynest.common.error.AccessDeniedBusinessException;
import com.handynest.common.error.BadRequestBusinessException;
import com.handynest.common.error.DuplicateResourceException;
import com.handynest.common.error.FeatureDisabledException;
import com.handynest.common.error.ResourceNotFoundException;
import com.handynest.common.idempotency.IdempotencyDecision;
import com.handynest.common.idempotency.IdempotencyService;
import com.handynest.identity.RoleName;
import com.handynest.identity.User;
import com.handynest.identity.UserProfileService;
import com.handynest.identity.UserRepository;
import com.handynest.marketplace.ChatMessageRepository;
import com.handynest.marketplace.DealRepository;
import com.handynest.marketplace.MarketplaceAttachmentRepository;
import com.handynest.marketplace.MarketplaceFeedbackRepository;
import com.handynest.marketplace.MarketplaceTaskRepository;
import com.handynest.performer.PerformerProfileRepository;
import com.handynest.platform.PlatformSettingKey;
import com.handynest.platform.PlatformSettingService;
import com.handynest.risk.CustomerRiskService;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ModerationService {

  private final UserProfileService userProfileService;
  private final UserRepository userRepository;
  private final MarketplaceTaskRepository taskRepository;
  private final DealRepository dealRepository;
  private final PerformerProfileRepository performerProfileRepository;
  private final ChatMessageRepository chatMessageRepository;
  private final CategoryQueryRepository categoryRepository;
  private final MarketplaceFeedbackRepository feedbackRepository;
  private final MarketplaceAttachmentRepository attachmentRepository;
  private final ComplaintRepository complaintRepository;
  private final ModerationCaseRepository moderationCaseRepository;
  private final CustomerRiskService customerRiskService;
  private final IdempotencyService idempotencyService;
  private final ObjectMapper objectMapper;
  private final PlatformSettingService platformSettingService;

  @Transactional
  public ComplaintResponse createComplaint(
      UserDetails userDetails, String idempotencyKey, ComplaintCreateRequest request) {
    User reporter = userProfileService.currentUser(userDetails);
    IdempotencyDecision idempotency =
        idempotencyService.begin(reporter, idempotencyKey, "/api/v1/complaints", toJson(request));
    if (idempotency.replay()) {
      String resourceId = idempotency.key().getResponseResourceId();
      return toComplaintResponse(
          complaintRepository
              .findByPublicId(resourceId)
              .orElseThrow(() -> new ResourceNotFoundException("Complaint", resourceId)));
    }
    User inferredTargetUser = resolveTargetUser(request.targetType(), request.targetId());
    User requestedTargetUser = findTargetUser(request.targetUserId());
    if (inferredTargetUser != null
        && requestedTargetUser != null
        && !inferredTargetUser.getId().equals(requestedTargetUser.getId())) {
      throw new BadRequestBusinessException("targetUserId does not match complaint target owner");
    }
    User targetUser = requestedTargetUser == null ? inferredTargetUser : requestedTargetUser;
    if (targetUser != null && targetUser.getId().equals(reporter.getId())) {
      throw new BadRequestBusinessException("Users cannot complain about themselves");
    }
    if (targetUser != null
        && performerProfileRepository.existsByUserId(reporter.getId())
        && !platformSettingService.booleanValue(PlatformSettingKey.PERFORMER_COMPLAINTS_ENABLED)) {
      throw new FeatureDisabledException("Performer complaints");
    }
    String targetId = request.targetId().trim();
    if (complaintRepository.existsByReporterIdAndTargetTypeAndTargetIdAndStatusIn(
        reporter.getId(),
        request.targetType(),
        targetId,
        List.of(ComplaintStatus.OPEN, ComplaintStatus.IN_REVIEW))) {
      throw new DuplicateResourceException("Active complaint already exists for this target");
    }

    Complaint complaint =
        new Complaint(
            reporter,
            targetUser,
            request.targetType(),
            targetId,
            request.reason().trim(),
            request.description().trim());
    ModerationCase moderationCase =
        new ModerationCase(
            request.targetType(),
            targetId,
            reporter,
            request.reason().trim(),
            ModerationPriority.NORMAL);
    moderationCase = moderationCaseRepository.save(moderationCase);
    complaint.attachModerationCase(moderationCase);
    complaint = complaintRepository.save(complaint);
    ComplaintResponse response = toComplaintResponse(complaint);
    idempotencyService.complete(
        idempotency.key(), 201, toJson(response), "COMPLAINT", complaint.getPublicId());
    return response;
  }

  @Transactional(readOnly = true)
  public PageResponse<ComplaintResponse> myComplaints(UserDetails userDetails, int page, int size) {
    User reporter = userProfileService.currentUser(userDetails);
    return PageResponse.from(
        complaintRepository
            .findAllByReporterIdOrderByCreatedAtDesc(
                reporter.getId(), pageable(page, size, Sort.Direction.DESC))
            .map(this::toComplaintResponse));
  }

  @Transactional
  public ComplaintResponse cancelComplaint(UserDetails userDetails, String complaintId) {
    User reporter = userProfileService.currentUser(userDetails);
    Complaint complaint =
        complaintRepository
            .findByPublicId(complaintId)
            .orElseThrow(() -> new ResourceNotFoundException("Complaint", complaintId));
    if (!complaint.getReporter().getId().equals(reporter.getId())) {
      throw new AccessDeniedBusinessException("Only complaint reporter can cancel it");
    }
    if (complaint.getStatus() != ComplaintStatus.OPEN) {
      throw new com.handynest.common.error.InvalidStatusTransitionException(
          "Complaint", complaint.getStatus().name(), ComplaintStatus.CANCELED.name());
    }
    complaint.cancel(Instant.now());
    if (complaint.getModerationCase() != null
        && complaint.getModerationCase().getStatus() == ModerationCaseStatus.OPEN) {
      complaint
          .getModerationCase()
          .close(
              ModerationCaseStatus.CANCELED,
              ModerationDecision.CANCELED,
              "Canceled by reporter",
              Instant.now());
    }
    return toComplaintResponse(complaint);
  }

  @Transactional(readOnly = true)
  public PageResponse<ModerationCaseResponse> adminCases(
      UserDetails userDetails,
      ModerationCaseStatus status,
      ModerationTargetType targetType,
      int page,
      int size) {
    User admin = userProfileService.currentUser(userDetails);
    assertSupport(admin);
    ModerationCaseStatus effectiveStatus = status == null ? ModerationCaseStatus.OPEN : status;
    Page<ModerationCase> cases =
        targetType == null
            ? moderationCaseRepository.findAllByStatusOrderByCreatedAtAsc(
                effectiveStatus, pageable(page, size, Sort.Direction.ASC))
            : moderationCaseRepository.findAllByTargetTypeAndStatusOrderByCreatedAtAsc(
                targetType, effectiveStatus, pageable(page, size, Sort.Direction.ASC));
    return PageResponse.from(cases.map(this::toModerationCaseResponse));
  }

  @Transactional(readOnly = true)
  public ModerationCaseResponse adminCase(UserDetails userDetails, String caseId) {
    User admin = userProfileService.currentUser(userDetails);
    assertSupport(admin);
    return toModerationCaseResponse(findCase(caseId));
  }

  @Transactional
  public ModerationCaseResponse startReview(UserDetails userDetails, String caseId) {
    User admin = userProfileService.currentUser(userDetails);
    assertAdmin(admin);
    ModerationCase moderationCase = findCaseForUpdate(caseId);
    moderationCase.startReview(admin);
    complaintsFor(moderationCase).forEach(Complaint::markInReview);
    return toModerationCaseResponse(moderationCase);
  }

  @Transactional
  public ModerationCaseResponse approve(
      UserDetails userDetails, String caseId, ModerationDecisionRequest request) {
    return close(
        userDetails, caseId, ModerationCaseStatus.APPROVED, ModerationDecision.APPROVED, request);
  }

  @Transactional
  public ModerationCaseResponse reject(
      UserDetails userDetails, String caseId, ModerationDecisionRequest request) {
    return close(
        userDetails, caseId, ModerationCaseStatus.REJECTED, ModerationDecision.REJECTED, request);
  }

  @Transactional
  public ModerationCaseResponse resolve(
      UserDetails userDetails, String caseId, ModerationDecisionRequest request) {
    return close(
        userDetails, caseId, ModerationCaseStatus.RESOLVED, ModerationDecision.RESOLVED, request);
  }

  @Transactional
  public ModerationCaseResponse cancel(
      UserDetails userDetails, String caseId, ModerationDecisionRequest request) {
    User admin = userProfileService.currentUser(userDetails);
    assertAdmin(admin);
    ModerationCase moderationCase = findCaseForUpdate(caseId);
    moderationCase.cancel(
        admin, request == null ? null : blankToNull(request.comment()), Instant.now());
    complaintsFor(moderationCase).forEach(complaint -> complaint.cancel(Instant.now()));
    return toModerationCaseResponse(moderationCase);
  }

  private ModerationCaseResponse close(
      UserDetails userDetails,
      String caseId,
      ModerationCaseStatus status,
      ModerationDecision decision,
      ModerationDecisionRequest request) {
    User admin = userProfileService.currentUser(userDetails);
    assertAdmin(admin);
    ModerationCase moderationCase = findCaseForUpdate(caseId);
    moderationCase.assertAssignedTo(admin);
    Instant resolvedAt = Instant.now();
    moderationCase.close(
        status, decision, request == null ? null : blankToNull(request.comment()), resolvedAt);
    complaintsFor(moderationCase)
        .forEach(
            complaint -> {
              complaint.markResolved(resolvedAt);
              if (decision == ModerationDecision.APPROVED) {
                customerRiskService.recordConfirmedComplaint(complaint);
              }
            });
    return toModerationCaseResponse(moderationCase);
  }

  private User resolveTargetUser(ModerationTargetType targetType, String targetId) {
    String normalizedTargetId = targetId.trim();
    return switch (targetType) {
      case TASK ->
          taskRepository
              .findByPublicId(normalizedTargetId)
              .orElseThrow(() -> new ResourceNotFoundException("Task", normalizedTargetId))
              .getCustomer();
      case PROFILE ->
          performerProfileRepository
              .findByPublicId(normalizedTargetId)
              .map(profile -> profile.getUser())
              .or(() -> userRepository.findByPublicId(normalizedTargetId))
              .orElseThrow(() -> new ResourceNotFoundException("Profile", normalizedTargetId));
      case CHAT_MESSAGE ->
          chatMessageRepository
              .findByPublicIdAndDeletedAtIsNull(normalizedTargetId)
              .orElseThrow(() -> new ResourceNotFoundException("ChatMessage", normalizedTargetId))
              .getSender();
      case CATEGORY -> {
        categoryRepository
            .findByPublicId(normalizedTargetId)
            .orElseThrow(() -> new ResourceNotFoundException("Category", normalizedTargetId));
        yield null;
      }
      case FEEDBACK ->
          feedbackRepository
              .findByPublicId(normalizedTargetId)
              .orElseThrow(() -> new ResourceNotFoundException("Feedback", normalizedTargetId))
              .getSender();
      case ATTACHMENT ->
          attachmentRepository
              .findByPublicIdAndDeletedAtIsNull(normalizedTargetId)
              .orElseThrow(() -> new ResourceNotFoundException("Attachment", normalizedTargetId))
              .getOwner();
      case DEAL -> {
        dealRepository
            .findByPublicId(normalizedTargetId)
            .orElseThrow(() -> new ResourceNotFoundException("Deal", normalizedTargetId));
        yield null;
      }
    };
  }

  private User findTargetUser(String targetUserId) {
    String normalizedTargetUserId = blankToNull(targetUserId);
    if (normalizedTargetUserId == null) {
      return null;
    }
    return userRepository
        .findByPublicId(normalizedTargetUserId)
        .orElseThrow(() -> new ResourceNotFoundException("User", normalizedTargetUserId));
  }

  private ModerationCase findCase(String caseId) {
    return moderationCaseRepository
        .findByPublicId(caseId)
        .orElseThrow(() -> new ResourceNotFoundException("ModerationCase", caseId));
  }

  private ModerationCase findCaseForUpdate(String caseId) {
    return moderationCaseRepository
        .findByPublicIdForUpdate(caseId)
        .orElseThrow(() -> new ResourceNotFoundException("ModerationCase", caseId));
  }

  private PageRequest pageable(int page, int size, Sort.Direction direction) {
    return PageRequest.of(
        Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by(direction, "createdAt"));
  }

  private String toJson(Object value) {
    try {
      return objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Failed to serialize moderation payload", exception);
    }
  }

  private List<Complaint> complaintsFor(ModerationCase moderationCase) {
    return complaintRepository.findAllByModerationCaseId(moderationCase.getId());
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
        complaint.getResolvedAt());
  }

  private ModerationCaseResponse toModerationCaseResponse(ModerationCase moderationCase) {
    return new ModerationCaseResponse(
        moderationCase.getPublicId(),
        moderationCase.getTargetType().name(),
        moderationCase.getTargetId(),
        moderationCase.getOpenedByUser() == null
            ? null
            : moderationCase.getOpenedByUser().getPublicId(),
        moderationCase.getAssignedAdmin() == null
            ? null
            : moderationCase.getAssignedAdmin().getPublicId(),
        moderationCase.getReason(),
        moderationCase.getStatus().name(),
        moderationCase.getPriority().name(),
        moderationCase.getDecision() == null ? null : moderationCase.getDecision().name(),
        moderationCase.getDecisionComment(),
        moderationCase.getCreatedAt(),
        moderationCase.getUpdatedAt(),
        moderationCase.getResolvedAt());
  }

  private String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value.trim();
  }
}
