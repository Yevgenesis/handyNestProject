package com.handynest.marketplace;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/tasks")
@RequiredArgsConstructor
public class AdminMarketplaceTaskApiV1Controller {

    private final MarketplaceService marketplaceService;

    @PostMapping("/{taskId}/publish")
    public MarketplaceTaskResponse publish(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String taskId
    ) {
        return marketplaceService.adminPublishTask(userDetails, taskId);
    }

    @PostMapping("/{taskId}/reject")
    public MarketplaceTaskResponse reject(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String taskId,
            @Valid @RequestBody(required = false) TaskModerationRejectRequest request
    ) {
        return marketplaceService.adminRejectTask(
                userDetails,
                taskId,
                request == null ? new TaskModerationRejectRequest(null) : request
        );
    }
}
