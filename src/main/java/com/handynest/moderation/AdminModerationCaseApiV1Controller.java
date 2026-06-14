package com.handynest.moderation;

import com.handynest.common.api.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@RequestMapping("/api/v1/admin/moderation-cases")
@Tag(name = "Admin Moderation", description = "Administrative moderation case review")
public class AdminModerationCaseApiV1Controller {

  private final ModerationService moderationService;

  @GetMapping
  @Operation(operationId = "adminListModerationCases", summary = "List moderation cases")
  public PageResponse<ModerationCaseResponse> list(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestParam(required = false) ModerationCaseStatus status,
      @RequestParam(required = false) ModerationTargetType targetType,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return moderationService.adminCases(userDetails, status, targetType, page, size);
  }

  @GetMapping("/{caseId}")
  @Operation(operationId = "adminGetModerationCase", summary = "Get moderation case")
  public ModerationCaseResponse find(
      @AuthenticationPrincipal UserDetails userDetails, @PathVariable String caseId) {
    return moderationService.adminCase(userDetails, caseId);
  }

  @PostMapping("/{caseId}/start-review")
  @Operation(operationId = "adminStartModerationReview", summary = "Start moderation review")
  public ModerationCaseResponse startReview(
      @AuthenticationPrincipal UserDetails userDetails, @PathVariable String caseId) {
    return moderationService.startReview(userDetails, caseId);
  }

  @PostMapping("/{caseId}/approve")
  @Operation(operationId = "adminApproveModerationCase", summary = "Approve moderation case")
  public ModerationCaseResponse approve(
      @AuthenticationPrincipal UserDetails userDetails,
      @PathVariable String caseId,
      @Valid @RequestBody(required = false) ModerationDecisionRequest request) {
    return moderationService.approve(userDetails, caseId, request);
  }

  @PostMapping("/{caseId}/reject")
  @Operation(operationId = "adminRejectModerationCase", summary = "Reject moderation case")
  public ModerationCaseResponse reject(
      @AuthenticationPrincipal UserDetails userDetails,
      @PathVariable String caseId,
      @Valid @RequestBody(required = false) ModerationDecisionRequest request) {
    return moderationService.reject(userDetails, caseId, request);
  }

  @PostMapping("/{caseId}/resolve")
  @Operation(operationId = "adminResolveModerationCase", summary = "Resolve moderation case")
  public ModerationCaseResponse resolve(
      @AuthenticationPrincipal UserDetails userDetails,
      @PathVariable String caseId,
      @Valid @RequestBody(required = false) ModerationDecisionRequest request) {
    return moderationService.resolve(userDetails, caseId, request);
  }

  @PostMapping("/{caseId}/cancel")
  @Operation(operationId = "adminCancelModerationCase", summary = "Cancel moderation case")
  public ModerationCaseResponse cancel(
      @AuthenticationPrincipal UserDetails userDetails,
      @PathVariable String caseId,
      @Valid @RequestBody(required = false) ModerationDecisionRequest request) {
    return moderationService.cancel(userDetails, caseId, request);
  }
}
