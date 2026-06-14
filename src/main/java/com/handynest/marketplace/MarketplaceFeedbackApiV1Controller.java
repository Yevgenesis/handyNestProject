package com.handynest.marketplace;

import com.handynest.common.api.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Feedback", description = "Marketplace feedback after completed deals.")
public class MarketplaceFeedbackApiV1Controller {

  private final MarketplaceService marketplaceService;

  @PostMapping("/tasks/{taskId}/feedbacks")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
      operationId = "createTaskFeedback",
      summary = "Create task feedback",
      description =
          "Creates feedback after a completed marketplace deal. Requires Idempotency-Key.")
  public FeedbackResponse createTaskFeedback(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestHeader(ApiConstants.IDEMPOTENCY_KEY_HEADER) String idempotencyKey,
      @Parameter(description = "Task publicId.") @PathVariable String taskId,
      @RequestBody @Valid FeedbackCreateRequest request) {
    return marketplaceService.createFeedback(userDetails, idempotencyKey, taskId, request);
  }

  @GetMapping("/tasks/{taskId}/feedbacks")
  @Operation(
      operationId = "listTaskFeedbacks",
      summary = "List task feedbacks",
      description = "Returns public visible feedbacks for a task publicId.")
  public List<FeedbackResponse> taskFeedbacks(
      @Parameter(description = "Task publicId.") @PathVariable String taskId) {
    return marketplaceService.taskFeedbacks(taskId);
  }

  @GetMapping("/users/{userId}/feedbacks")
  @Operation(
      operationId = "listUserFeedbacks",
      summary = "List user feedbacks",
      description = "Returns public visible feedbacks received by a user publicId.")
  public List<FeedbackResponse> userFeedbacks(
      @Parameter(description = "User publicId.") @PathVariable String userId) {
    return marketplaceService.userFeedbacks(userId);
  }

  @GetMapping("/performers/{performerId}/feedbacks")
  @Operation(
      operationId = "listPerformerFeedbacks",
      summary = "List performer feedbacks",
      description = "Returns public visible feedbacks for a performer profile publicId.")
  public List<FeedbackResponse> performerFeedbacks(
      @Parameter(description = "Performer profile publicId.") @PathVariable String performerId) {
    return marketplaceService.performerFeedbacks(performerId);
  }
}
