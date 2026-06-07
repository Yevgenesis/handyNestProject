package com.handynest.marketplace;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class MarketplaceFeedbackApiV1Controller {

    private final MarketplaceService marketplaceService;

    @PostMapping("/tasks/{taskId}/feedbacks")
    @ResponseStatus(HttpStatus.CREATED)
    public FeedbackResponse createTaskFeedback(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @PathVariable String taskId,
            @RequestBody @Valid FeedbackCreateRequest request
    ) {
        return marketplaceService.createFeedback(userDetails, idempotencyKey, taskId, request);
    }

    @GetMapping("/tasks/{taskId}/feedbacks")
    public List<FeedbackResponse> taskFeedbacks(@PathVariable String taskId) {
        return marketplaceService.taskFeedbacks(taskId);
    }

    @GetMapping("/users/{userId}/feedbacks")
    public List<FeedbackResponse> userFeedbacks(@PathVariable String userId) {
        return marketplaceService.userFeedbacks(userId);
    }

    @GetMapping("/performers/{performerId}/feedbacks")
    public List<FeedbackResponse> performerFeedbacks(@PathVariable String performerId) {
        return marketplaceService.performerFeedbacks(performerId);
    }
}
