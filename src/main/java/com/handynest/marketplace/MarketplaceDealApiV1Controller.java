package com.handynest.marketplace;

import java.util.List;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MarketplaceDealApiV1Controller {

    private final MarketplaceService marketplaceService;

    @GetMapping("/api/v1/deals/{dealId}")
    public DealResponse findDeal(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String dealId
    ) {
        return marketplaceService.findDeal(userDetails, dealId);
    }

    @GetMapping("/api/v1/my/deals")
    public List<DealResponse> myDeals(@AuthenticationPrincipal UserDetails userDetails) {
        return marketplaceService.myDeals(userDetails);
    }

    @PostMapping("/api/v1/deals/{dealId}/milestones")
    @ResponseStatus(HttpStatus.CREATED)
    public MilestoneResponse createMilestone(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @PathVariable String dealId,
            @Valid @RequestBody MilestoneCreateRequest request
    ) {
        return marketplaceService.createMilestone(userDetails, idempotencyKey, dealId, request);
    }

    @GetMapping("/api/v1/deals/{dealId}/milestones")
    public List<MilestoneResponse> listDealMilestones(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String dealId
    ) {
        return marketplaceService.listDealMilestones(userDetails, dealId);
    }

    @GetMapping("/api/v1/milestones/{milestoneId}")
    public MilestoneResponse findMilestone(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String milestoneId
    ) {
        return marketplaceService.findMilestone(userDetails, milestoneId);
    }

    @PostMapping("/api/v1/milestones/{milestoneId}/start")
    public MilestoneResponse startMilestone(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @PathVariable String milestoneId
    ) {
        return marketplaceService.startMilestone(userDetails, idempotencyKey, milestoneId);
    }

    @PostMapping("/api/v1/milestones/{milestoneId}/submit")
    public MilestoneResponse submitMilestone(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @PathVariable String milestoneId,
            @Valid @RequestBody(required = false) MilestoneDecisionRequest request
    ) {
        return marketplaceService.submitMilestone(userDetails, idempotencyKey, milestoneId, request);
    }

    @PostMapping("/api/v1/milestones/{milestoneId}/accept")
    public MilestoneResponse acceptMilestone(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @PathVariable String milestoneId,
            @Valid @RequestBody(required = false) MilestoneDecisionRequest request
    ) {
        return marketplaceService.acceptMilestone(userDetails, idempotencyKey, milestoneId, request);
    }

    @PostMapping("/api/v1/milestones/{milestoneId}/reject")
    public MilestoneResponse rejectMilestone(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @PathVariable String milestoneId,
            @Valid @RequestBody MilestoneDecisionRequest request
    ) {
        return marketplaceService.rejectMilestone(userDetails, idempotencyKey, milestoneId, request);
    }

    @PostMapping("/api/v1/milestones/{milestoneId}/dispute")
    public MilestoneResponse disputeMilestone(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @PathVariable String milestoneId,
            @Valid @RequestBody(required = false) MilestoneDecisionRequest request
    ) {
        return marketplaceService.disputeMilestone(userDetails, idempotencyKey, milestoneId, request);
    }

    @PostMapping("/api/v1/milestones/{milestoneId}/cancel")
    public MilestoneResponse cancelMilestone(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @PathVariable String milestoneId,
            @Valid @RequestBody(required = false) MilestoneDecisionRequest request
    ) {
        return marketplaceService.cancelMilestone(userDetails, idempotencyKey, milestoneId, request);
    }

    @GetMapping("/api/v1/my/offers")
    public List<TaskOfferResponse> myOffers(@AuthenticationPrincipal UserDetails userDetails) {
        return marketplaceService.myOffers(userDetails);
    }
}
