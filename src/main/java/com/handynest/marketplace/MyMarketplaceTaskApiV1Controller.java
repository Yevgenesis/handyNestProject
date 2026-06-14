package com.handynest.marketplace;

import com.handynest.common.api.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/my/tasks")
@Tag(name = "Marketplace Tasks", description = "Current customer's marketplace tasks.")
public class MyMarketplaceTaskApiV1Controller {

  private final MarketplaceService marketplaceService;

  @GetMapping
  @Operation(
      operationId = "listMyMarketplaceTasks",
      summary = "List current customer's tasks",
      description = "Returns tasks owned by the authenticated customer.")
  public PageResponse<MarketplaceTaskResponse> myTasks(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestParam(required = false) TaskStatus status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return marketplaceService.myTasks(userDetails, status, page, size);
  }
}
