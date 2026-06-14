package com.handynest.marketplace;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(
    name = "Admin Marketplace",
    description = "Administrative marketplace task moderation actions.")
public class AdminMarketplaceTaskApiV1Controller {

  private final MarketplaceService marketplaceService;

  @PostMapping("/{taskId}/publish")
  @Operation(
      operationId = "adminPublishMarketplaceTask",
      summary = "Publish task by moderation",
      description = "Admin action that publishes a moderated task by task publicId.")
  public MarketplaceTaskResponse publish(
      @AuthenticationPrincipal UserDetails userDetails,
      @Parameter(description = "Task publicId.") @PathVariable String taskId) {
    return marketplaceService.adminPublishTask(userDetails, taskId);
  }

  @PostMapping("/{taskId}/reject")
  @Operation(
      operationId = "adminRejectMarketplaceTask",
      summary = "Reject task by moderation",
      description = "Admin action that rejects a moderated task by task publicId.")
  public MarketplaceTaskResponse reject(
      @AuthenticationPrincipal UserDetails userDetails,
      @Parameter(description = "Task publicId.") @PathVariable String taskId,
      @Valid @RequestBody(required = false) TaskModerationRejectRequest request) {
    return marketplaceService.adminRejectTask(
        userDetails, taskId, request == null ? new TaskModerationRejectRequest(null) : request);
  }
}
