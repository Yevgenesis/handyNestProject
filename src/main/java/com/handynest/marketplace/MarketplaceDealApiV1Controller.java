package com.handynest.marketplace;

import com.handynest.common.api.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Deals", description = "Deal, offer inbox and milestone operations for accepted tasks.")
public class MarketplaceDealApiV1Controller {

  private final MarketplaceService marketplaceService;

  @GetMapping("/api/v1/deals/{dealId}")
  @Operation(
      operationId = "getDeal",
      summary = "Get deal",
      description = "Returns a deal by publicId for a participant.")
  public DealResponse findDeal(
      @AuthenticationPrincipal UserDetails userDetails,
      @Parameter(description = "Deal publicId.") @PathVariable String dealId) {
    return marketplaceService.findDeal(userDetails, dealId);
  }

  @GetMapping("/api/v1/my/deals")
  @Operation(
      operationId = "listMyDeals",
      summary = "List my deals",
      description = "Returns deals where the authenticated user is a customer or performer.")
  public List<DealResponse> myDeals(@AuthenticationPrincipal UserDetails userDetails) {
    return marketplaceService.myDeals(userDetails);
  }

  @PostMapping("/api/v1/deals/{dealId}/contact-reveals")
  @Operation(
      operationId = "revealDealContact",
      summary = "Reveal a verified participant contact",
      description =
          "Reveals the other participant's verified phone when category and deal rules allow it. "
              + "The raw value is returned only by this response. Requires Idempotency-Key.")
  public ContactRevealResponse revealContact(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestHeader(ApiConstants.IDEMPOTENCY_KEY_HEADER) String idempotencyKey,
      @Parameter(description = "Deal publicId.") @PathVariable String dealId,
      @Valid @RequestBody ContactRevealRequest request,
      HttpServletRequest httpRequest) {
    return marketplaceService.revealContact(
        userDetails,
        idempotencyKey,
        dealId,
        request,
        clientIp(httpRequest),
        httpRequest.getHeader("User-Agent"));
  }

  @GetMapping("/api/v1/deals/{dealId}/contact-reveals")
  @Operation(
      operationId = "listDealContactReveals",
      summary = "List contact reveal audit history",
      description = "Returns reveal metadata without raw contact values.")
  public List<ContactRevealHistoryResponse> contactRevealHistory(
      @AuthenticationPrincipal UserDetails userDetails,
      @Parameter(description = "Deal publicId.") @PathVariable String dealId) {
    return marketplaceService.contactRevealHistory(userDetails, dealId);
  }

  @PostMapping("/api/v1/deals/{dealId}/cancel")
  @Operation(
      operationId = "cancelDeal",
      summary = "Cancel an active deal",
      description =
          "Cancels an ACTIVE or REVISION_REQUESTED deal atomically with its task and chat. "
              + "Requires Idempotency-Key.")
  public DealResponse cancelDeal(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestHeader(ApiConstants.IDEMPOTENCY_KEY_HEADER) String idempotencyKey,
      @Parameter(description = "Deal publicId.") @PathVariable String dealId,
      @Valid @RequestBody DealCancelRequest request,
      HttpServletRequest httpRequest) {
    return marketplaceService.cancelDeal(
        userDetails,
        idempotencyKey,
        dealId,
        request,
        clientIp(httpRequest),
        httpRequest.getHeader("User-Agent"));
  }

  @PostMapping("/api/v1/deals/{dealId}/milestones")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
      operationId = "createDealMilestone",
      summary = "Create deal milestone",
      description = "Creates a milestone for a deal. Requires Idempotency-Key.")
  public MilestoneResponse createMilestone(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestHeader(ApiConstants.IDEMPOTENCY_KEY_HEADER) String idempotencyKey,
      @Parameter(description = "Deal publicId.") @PathVariable String dealId,
      @Valid @RequestBody MilestoneCreateRequest request) {
    return marketplaceService.createMilestone(userDetails, idempotencyKey, dealId, request);
  }

  @GetMapping("/api/v1/deals/{dealId}/milestones")
  @Operation(
      operationId = "listDealMilestones",
      summary = "List deal milestones",
      description = "Returns milestones for a deal publicId.")
  public List<MilestoneResponse> listDealMilestones(
      @AuthenticationPrincipal UserDetails userDetails,
      @Parameter(description = "Deal publicId.") @PathVariable String dealId) {
    return marketplaceService.listDealMilestones(userDetails, dealId);
  }

  @GetMapping("/api/v1/milestones/{milestoneId}")
  @Operation(
      operationId = "getMilestone",
      summary = "Get milestone",
      description = "Returns one milestone by publicId.")
  public MilestoneResponse findMilestone(
      @AuthenticationPrincipal UserDetails userDetails,
      @Parameter(description = "Milestone publicId.") @PathVariable String milestoneId) {
    return marketplaceService.findMilestone(userDetails, milestoneId);
  }

  @PostMapping("/api/v1/milestones/{milestoneId}/start")
  @Operation(
      operationId = "startMilestone",
      summary = "Start milestone",
      description = "Moves a milestone into progress. Requires Idempotency-Key.")
  public MilestoneResponse startMilestone(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestHeader(ApiConstants.IDEMPOTENCY_KEY_HEADER) String idempotencyKey,
      @Parameter(description = "Milestone publicId.") @PathVariable String milestoneId) {
    return marketplaceService.startMilestone(userDetails, idempotencyKey, milestoneId);
  }

  @PostMapping("/api/v1/milestones/{milestoneId}/submit")
  @Operation(
      operationId = "submitMilestone",
      summary = "Submit milestone",
      description = "Submits milestone work for customer review. Requires Idempotency-Key.")
  public MilestoneResponse submitMilestone(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestHeader(ApiConstants.IDEMPOTENCY_KEY_HEADER) String idempotencyKey,
      @Parameter(description = "Milestone publicId.") @PathVariable String milestoneId,
      @Valid @RequestBody(required = false) MilestoneDecisionRequest request) {
    return marketplaceService.submitMilestone(userDetails, idempotencyKey, milestoneId, request);
  }

  @PostMapping("/api/v1/milestones/{milestoneId}/accept")
  @Operation(
      operationId = "acceptMilestone",
      summary = "Accept milestone",
      description = "Accepts submitted milestone work. Requires Idempotency-Key.")
  public MilestoneResponse acceptMilestone(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestHeader(ApiConstants.IDEMPOTENCY_KEY_HEADER) String idempotencyKey,
      @Parameter(description = "Milestone publicId.") @PathVariable String milestoneId,
      @Valid @RequestBody(required = false) MilestoneDecisionRequest request) {
    return marketplaceService.acceptMilestone(userDetails, idempotencyKey, milestoneId, request);
  }

  @PostMapping("/api/v1/milestones/{milestoneId}/reject")
  @Operation(
      operationId = "rejectMilestone",
      summary = "Reject milestone",
      description = "Rejects submitted milestone work. Requires Idempotency-Key.")
  public MilestoneResponse rejectMilestone(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestHeader(ApiConstants.IDEMPOTENCY_KEY_HEADER) String idempotencyKey,
      @Parameter(description = "Milestone publicId.") @PathVariable String milestoneId,
      @Valid @RequestBody MilestoneDecisionRequest request) {
    return marketplaceService.rejectMilestone(userDetails, idempotencyKey, milestoneId, request);
  }

  @PostMapping("/api/v1/milestones/{milestoneId}/dispute")
  @Operation(
      operationId = "disputeMilestone",
      summary = "Dispute milestone",
      description = "Opens a milestone dispute. Requires Idempotency-Key.")
  public MilestoneResponse disputeMilestone(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestHeader(ApiConstants.IDEMPOTENCY_KEY_HEADER) String idempotencyKey,
      @Parameter(description = "Milestone publicId.") @PathVariable String milestoneId,
      @Valid @RequestBody(required = false) MilestoneDecisionRequest request) {
    return marketplaceService.disputeMilestone(userDetails, idempotencyKey, milestoneId, request);
  }

  @PostMapping("/api/v1/milestones/{milestoneId}/cancel")
  @Operation(
      operationId = "cancelMilestone",
      summary = "Cancel milestone",
      description = "Cancels a milestone. Requires Idempotency-Key.")
  public MilestoneResponse cancelMilestone(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestHeader(ApiConstants.IDEMPOTENCY_KEY_HEADER) String idempotencyKey,
      @Parameter(description = "Milestone publicId.") @PathVariable String milestoneId,
      @Valid @RequestBody(required = false) MilestoneDecisionRequest request) {
    return marketplaceService.cancelMilestone(userDetails, idempotencyKey, milestoneId, request);
  }

  @GetMapping("/api/v1/my/offers")
  @Operation(
      operationId = "listMyTaskOffers",
      summary = "List my task offers",
      description = "Returns offers created by the authenticated performer.")
  public List<TaskOfferResponse> myOffers(@AuthenticationPrincipal UserDetails userDetails) {
    return marketplaceService.myOffers(userDetails);
  }

  private String clientIp(HttpServletRequest request) {
    String forwardedFor = request.getHeader("X-Forwarded-For");
    if (forwardedFor != null && !forwardedFor.isBlank()) {
      return forwardedFor.split(",", 2)[0].trim();
    }
    return request.getRemoteAddr();
  }
}
