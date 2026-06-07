package com.handynest.marketplace;

import java.util.List;
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
public class MarketplaceDisputeApiV1Controller {

    private final MarketplaceService marketplaceService;

    @GetMapping("/disputes/{disputeId}")
    public DisputeCaseResponse findDispute(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String disputeId
    ) {
        return marketplaceService.findDispute(userDetails, disputeId);
    }

    @GetMapping("/my/disputes")
    public List<DisputeCaseResponse> myDisputes(@AuthenticationPrincipal UserDetails userDetails) {
        return marketplaceService.myDisputes(userDetails);
    }

    @GetMapping("/admin/disputes")
    public List<DisputeCaseResponse> adminDisputes(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) DisputeCaseStatus status
    ) {
        return marketplaceService.adminDisputes(userDetails, status);
    }

    @PostMapping("/admin/disputes/{disputeId}/resolve")
    public DisputeCaseResponse adminResolveDispute(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @PathVariable String disputeId,
            @Valid @RequestBody DisputeResolutionRequest request
    ) {
        return marketplaceService.adminResolveDispute(userDetails, idempotencyKey, disputeId, request);
    }
}
