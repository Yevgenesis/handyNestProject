package com.handynest.performer;

import com.handynest.common.error.AccessDeniedBusinessException;
import com.handynest.common.error.BadRequestBusinessException;
import com.handynest.common.error.InvalidStatusTransitionException;
import com.handynest.common.error.ResourceNotFoundException;
import com.handynest.identity.RoleName;
import com.handynest.identity.User;
import com.handynest.identity.UserProfileService;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PerformerCategoryModerationService {

  private final PerformerCategoryRepository performerCategoryRepository;
  private final UserProfileService userProfileService;

  @Transactional(readOnly = true)
  public List<PerformerCategoryModerationResponse> list(
      UserDetails userDetails, PerformerCategoryApprovalStatus status) {
    assertAdmin(userProfileService.currentUser(userDetails));
    PerformerCategoryApprovalStatus effectiveStatus =
        status == null ? PerformerCategoryApprovalStatus.PENDING : status;
    return performerCategoryRepository
        .findAllByApprovalStatusOrderByCreatedAtAsc(effectiveStatus)
        .stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional
  public PerformerCategoryModerationResponse approve(
      UserDetails userDetails, String performerId, String categoryId) {
    User admin = userProfileService.currentUser(userDetails);
    assertAdmin(admin);
    PerformerCategory performerCategory = findCategory(performerId, categoryId);
    assertManualReviewRequired(performerCategory);
    if (performerCategory.getApprovalStatus() == PerformerCategoryApprovalStatus.APPROVED) {
      throw new InvalidStatusTransitionException("PerformerCategory", "APPROVED", "APPROVED");
    }
    performerCategory.approve(admin, Instant.now());
    return toResponse(performerCategory);
  }

  @Transactional
  public PerformerCategoryModerationResponse reject(
      UserDetails userDetails,
      String performerId,
      String categoryId,
      PerformerCategoryDecisionRequest request) {
    User admin = userProfileService.currentUser(userDetails);
    assertAdmin(admin);
    String reason = request == null ? null : blankToNull(request.reason());
    if (reason == null) {
      throw new BadRequestBusinessException("rejection reason is required");
    }
    PerformerCategory performerCategory = findCategory(performerId, categoryId);
    assertManualReviewRequired(performerCategory);
    if (performerCategory.getApprovalStatus() != PerformerCategoryApprovalStatus.PENDING) {
      throw new InvalidStatusTransitionException(
          "PerformerCategory", performerCategory.getApprovalStatus().name(), "REJECTED");
    }
    performerCategory.reject(admin, reason, Instant.now());
    return toResponse(performerCategory);
  }

  private PerformerCategory findCategory(String performerId, String categoryId) {
    return performerCategoryRepository
        .findByPerformerProfilePublicIdAndCategoryPublicId(performerId, categoryId)
        .orElseThrow(
            () ->
                new ResourceNotFoundException("PerformerCategory", performerId + ":" + categoryId));
  }

  private void assertManualReviewRequired(PerformerCategory performerCategory) {
    if (!performerCategory.getCategory().isRequiresManualApproval()
        && !performerCategory.getCategory().isRequiresLicense()) {
      throw new BadRequestBusinessException("Category does not require manual approval");
    }
  }

  private void assertAdmin(User user) {
    if (!user.getRoles().contains(RoleName.ADMIN)) {
      throw new AccessDeniedBusinessException("Admin role is required");
    }
  }

  private PerformerCategoryModerationResponse toResponse(PerformerCategory performerCategory) {
    return new PerformerCategoryModerationResponse(
        performerCategory.getPerformerProfile().getPublicId(),
        performerCategory.getCategory().getPublicId(),
        performerCategory.getCategory().getTitle(),
        performerCategory.getApprovalStatus(),
        performerCategory.getReviewedBy() == null
            ? null
            : performerCategory.getReviewedBy().getPublicId(),
        performerCategory.getReviewedAt(),
        performerCategory.getRejectionReason());
  }

  private String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value.trim();
  }
}
