package com.handynest.performer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
@Tag(
    name = "Admin Verification",
    description = "Manual performer access approval for risk categories.")
public class AdminPerformerCategoryApiV1Controller {

  private final PerformerCategoryModerationService moderationService;

  @GetMapping("/performer-categories")
  @Operation(
      operationId = "listPerformerCategoryApprovals",
      summary = "List performer category approval records")
  public List<PerformerCategoryModerationResponse> list(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestParam(required = false) PerformerCategoryApprovalStatus status) {
    return moderationService.list(userDetails, status);
  }

  @PostMapping("/performers/{performerId}/categories/{categoryId}/approve")
  @Operation(
      operationId = "approvePerformerCategory",
      summary = "Approve performer access to a manually moderated category")
  public PerformerCategoryModerationResponse approve(
      @AuthenticationPrincipal UserDetails userDetails,
      @PathVariable String performerId,
      @PathVariable String categoryId) {
    return moderationService.approve(userDetails, performerId, categoryId);
  }

  @PostMapping("/performers/{performerId}/categories/{categoryId}/reject")
  @Operation(
      operationId = "rejectPerformerCategory",
      summary = "Reject performer access to a manually moderated category")
  public PerformerCategoryModerationResponse reject(
      @AuthenticationPrincipal UserDetails userDetails,
      @PathVariable String performerId,
      @PathVariable String categoryId,
      @Valid @RequestBody PerformerCategoryDecisionRequest request) {
    return moderationService.reject(userDetails, performerId, categoryId, request);
  }
}
