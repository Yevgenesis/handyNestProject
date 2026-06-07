package com.handynest.marketplace;

import com.handynest.catalog.category.CategoryServiceMode;
import com.handynest.common.api.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class MarketplaceTaskApiV1Controller {

    private final MarketplaceService marketplaceService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MarketplaceTaskResponse create(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody TaskCreateRequest request
    ) {
        return marketplaceService.createTask(userDetails, idempotencyKey, request);
    }

    @GetMapping
    public PageResponse<MarketplaceTaskResponse> search(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String cityId,
            @RequestParam(required = false) CategoryServiceMode serviceMode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return marketplaceService.searchTasks(status, categoryId, cityId, serviceMode, page, size);
    }

    @GetMapping("/{taskId}")
    public MarketplaceTaskResponse find(@PathVariable String taskId) {
        return marketplaceService.findTask(taskId);
    }

    @PostMapping("/{taskId}/repeat")
    @ResponseStatus(HttpStatus.CREATED)
    public MarketplaceTaskResponse repeat(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @PathVariable String taskId
    ) {
        return marketplaceService.repeatTask(userDetails, idempotencyKey, taskId);
    }

    @PostMapping("/{taskId}/cancel")
    public MarketplaceTaskResponse cancel(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String taskId
    ) {
        return marketplaceService.cancelTask(userDetails, taskId);
    }

    @PostMapping("/{taskId}/offers")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskOfferResponse createOffer(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @PathVariable String taskId,
            @Valid @RequestBody TaskOfferCreateRequest request
    ) {
        return marketplaceService.createOffer(userDetails, idempotencyKey, taskId, request);
    }

    @GetMapping("/{taskId}/offers")
    public java.util.List<TaskOfferResponse> listOffers(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String taskId
    ) {
        return marketplaceService.listTaskOffers(userDetails, taskId);
    }

    @PostMapping("/{taskId}/offers/{offerId}/accept")
    public DealResponse acceptOffer(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @PathVariable String taskId,
            @PathVariable String offerId
    ) {
        return marketplaceService.acceptOffer(userDetails, idempotencyKey, taskId, offerId);
    }
}
