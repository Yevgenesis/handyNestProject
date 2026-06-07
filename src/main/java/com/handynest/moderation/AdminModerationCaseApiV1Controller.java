package com.handynest.moderation;

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
@RequestMapping("/api/v1/admin/moderation-cases")
public class AdminModerationCaseApiV1Controller {

    private final ModerationService moderationService;

    @GetMapping
    public List<ModerationCaseResponse> list(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) ModerationCaseStatus status,
            @RequestParam(required = false) ModerationTargetType targetType
    ) {
        return moderationService.adminCases(userDetails, status, targetType);
    }

    @GetMapping("/{caseId}")
    public ModerationCaseResponse find(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String caseId
    ) {
        return moderationService.adminCase(userDetails, caseId);
    }

    @PostMapping("/{caseId}/start-review")
    public ModerationCaseResponse startReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String caseId
    ) {
        return moderationService.startReview(userDetails, caseId);
    }

    @PostMapping("/{caseId}/approve")
    public ModerationCaseResponse approve(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String caseId,
            @Valid @RequestBody(required = false) ModerationDecisionRequest request
    ) {
        return moderationService.approve(userDetails, caseId, request);
    }

    @PostMapping("/{caseId}/reject")
    public ModerationCaseResponse reject(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String caseId,
            @Valid @RequestBody(required = false) ModerationDecisionRequest request
    ) {
        return moderationService.reject(userDetails, caseId, request);
    }

    @PostMapping("/{caseId}/resolve")
    public ModerationCaseResponse resolve(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String caseId,
            @Valid @RequestBody(required = false) ModerationDecisionRequest request
    ) {
        return moderationService.resolve(userDetails, caseId, request);
    }
}
