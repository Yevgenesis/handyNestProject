package com.handynest.verification;

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
@RequestMapping("/api/v1/admin/verification-requests")
public class AdminVerificationRequestApiV1Controller {

    private final VerificationRequestService verificationRequestService;

    @GetMapping
    public List<VerificationRequestResponse> list(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) VerificationRequestStatus status
    ) {
        return verificationRequestService.adminList(userDetails, status);
    }

    @PostMapping("/{requestId}/approve")
    public VerificationRequestResponse approve(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String requestId,
            @Valid @RequestBody(required = false) VerificationDecisionRequest request
    ) {
        return verificationRequestService.approve(userDetails, requestId, request);
    }

    @PostMapping("/{requestId}/reject")
    public VerificationRequestResponse reject(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String requestId,
            @Valid @RequestBody(required = false) VerificationDecisionRequest request
    ) {
        return verificationRequestService.reject(userDetails, requestId, request);
    }
}
