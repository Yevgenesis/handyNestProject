package com.handynest.marketplace;

import com.handynest.common.api.ApiConstants;
import com.handynest.common.api.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Disputes", description = "Marketplace dispute lookup and admin resolution.")
public class MarketplaceDisputeApiV1Controller {

  private final MarketplaceService marketplaceService;

  @GetMapping("/disputes/{disputeId}")
  @Operation(
      operationId = "getDispute",
      summary = "Get dispute",
      description = "Returns a dispute case by publicId for participants or admins.")
  public DisputeCaseResponse findDispute(
      @AuthenticationPrincipal UserDetails userDetails,
      @Parameter(description = "Dispute case publicId.") @PathVariable String disputeId) {
    return marketplaceService.findDispute(userDetails, disputeId);
  }

  @GetMapping("/my/disputes")
  @Operation(
      operationId = "listMyDisputes",
      summary = "List my disputes",
      description = "Returns disputes opened by or involving the authenticated user.")
  public PageResponse<DisputeCaseResponse> myDisputes(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return marketplaceService.myDisputes(userDetails, page, size);
  }

  @GetMapping("/admin/disputes")
  @Operation(
      operationId = "adminListDisputes",
      summary = "List disputes for admin",
      description = "Returns dispute cases for administrative review.")
  public PageResponse<DisputeCaseResponse> adminDisputes(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestParam(required = false) DisputeCaseStatus status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return marketplaceService.adminDisputes(userDetails, status, page, size);
  }

  @PostMapping("/admin/disputes/{disputeId}/start-review")
  @Operation(operationId = "adminStartDisputeReview", summary = "Start dispute review")
  public DisputeCaseResponse startReview(
      @AuthenticationPrincipal UserDetails userDetails,
      @PathVariable String disputeId,
      @Valid @RequestBody(required = false) DisputeReviewRequest request) {
    return marketplaceService.adminStartDisputeReview(userDetails, disputeId, request);
  }

  @PostMapping("/admin/disputes/{disputeId}/request-customer-evidence")
  @Operation(
      operationId = "adminRequestCustomerEvidence",
      summary = "Request evidence from customer")
  public DisputeCaseResponse requestCustomerEvidence(
      @AuthenticationPrincipal UserDetails userDetails,
      @PathVariable String disputeId,
      @Valid @RequestBody(required = false) DisputeReviewRequest request) {
    return marketplaceService.adminRequestDisputeEvidence(userDetails, disputeId, true, request);
  }

  @PostMapping("/admin/disputes/{disputeId}/request-performer-evidence")
  @Operation(
      operationId = "adminRequestPerformerEvidence",
      summary = "Request evidence from performer")
  public DisputeCaseResponse requestPerformerEvidence(
      @AuthenticationPrincipal UserDetails userDetails,
      @PathVariable String disputeId,
      @Valid @RequestBody(required = false) DisputeReviewRequest request) {
    return marketplaceService.adminRequestDisputeEvidence(userDetails, disputeId, false, request);
  }

  @PostMapping("/admin/disputes/{disputeId}/resolve")
  @Operation(
      operationId = "adminResolveDispute",
      summary = "Resolve dispute",
      description = "Admin resolution action for a dispute case. Requires Idempotency-Key.")
  public DisputeCaseResponse adminResolveDispute(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestHeader(ApiConstants.IDEMPOTENCY_KEY_HEADER) String idempotencyKey,
      @Parameter(description = "Dispute case publicId.") @PathVariable String disputeId,
      @Valid @RequestBody DisputeResolutionRequest request) {
    return marketplaceService.adminResolveDispute(userDetails, idempotencyKey, disputeId, request);
  }
}
