package com.handynest.marketplace;

import com.handynest.catalog.category.CategoryServiceMode;
import com.handynest.common.api.ApiConstants;
import com.handynest.common.api.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(
    name = "Marketplace Tasks",
    description = "Task publication, search, offers and acceptance flow.")
public class MarketplaceTaskApiV1Controller {

  private final MarketplaceService marketplaceService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
      operationId = "createMarketplaceTask",
      summary = "Create marketplace task",
      description = "Creates an immediately published MVP task. Requires Idempotency-Key.")
  public MarketplaceTaskResponse create(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestHeader(ApiConstants.IDEMPOTENCY_KEY_HEADER) String idempotencyKey,
      @Valid @RequestBody TaskCreateRequest request) {
    return marketplaceService.createTask(userDetails, idempotencyKey, request);
  }

  @GetMapping
  @Operation(
      operationId = "searchMarketplaceTasks",
      summary = "Search public marketplace tasks",
      description = "Returns public tasks using public category and geo ids.")
  public PageResponse<MarketplaceTaskResponse> search(
      @RequestParam(required = false) TaskStatus status,
      @Parameter(description = "Category publicId.") @RequestParam(required = false)
          String categoryId,
      @Parameter(description = "City publicId.") @RequestParam(required = false) String cityId,
      @Parameter(description = "ISO country code. Defaults to the active launch market.")
          @RequestParam(required = false)
          String countryCode,
      @RequestParam(required = false) CategoryServiceMode serviceMode,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return marketplaceService.searchTasks(
        status, categoryId, cityId, countryCode, serviceMode, page, size);
  }

  @GetMapping("/{taskId}")
  @Operation(
      operationId = "getMarketplaceTask",
      summary = "Get public marketplace task",
      description = "Returns one task by task publicId.")
  public MarketplaceTaskResponse find(
      @Parameter(description = "Task publicId.") @PathVariable String taskId) {
    return marketplaceService.findTask(taskId);
  }

  @PostMapping("/{taskId}/repeat")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
      operationId = "repeatMarketplaceTask",
      summary = "Repeat marketplace task",
      description = "Creates a new task from an existing task publicId. Requires Idempotency-Key.")
  public MarketplaceTaskResponse repeat(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestHeader(ApiConstants.IDEMPOTENCY_KEY_HEADER) String idempotencyKey,
      @Parameter(description = "Source task publicId.") @PathVariable String taskId) {
    return marketplaceService.repeatTask(userDetails, idempotencyKey, taskId);
  }

  @PostMapping("/{taskId}/cancel")
  @Operation(
      operationId = "cancelMarketplaceTask",
      summary = "Cancel open marketplace task",
      description = "Cancels an OPEN task owned by the authenticated customer.")
  public MarketplaceTaskResponse cancel(
      @AuthenticationPrincipal UserDetails userDetails,
      @Parameter(description = "Task publicId.") @PathVariable String taskId) {
    return marketplaceService.cancelTask(userDetails, taskId);
  }

  @PostMapping("/{taskId}/offers")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
      operationId = "createTaskOffer",
      summary = "Create task offer",
      description = "Creates a performer offer for an OPEN task. Requires Idempotency-Key.")
  public TaskOfferResponse createOffer(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestHeader(ApiConstants.IDEMPOTENCY_KEY_HEADER) String idempotencyKey,
      @Parameter(description = "Task publicId.") @PathVariable String taskId,
      @Valid @RequestBody TaskOfferCreateRequest request) {
    return marketplaceService.createOffer(userDetails, idempotencyKey, taskId, request);
  }

  @GetMapping("/{taskId}/offers")
  @Operation(
      operationId = "listTaskOffers",
      summary = "List task offers",
      description = "Returns offers for a task visible to the task owner.")
  public java.util.List<TaskOfferResponse> listOffers(
      @AuthenticationPrincipal UserDetails userDetails,
      @Parameter(description = "Task publicId.") @PathVariable String taskId) {
    return marketplaceService.listTaskOffers(userDetails, taskId);
  }

  @PostMapping("/{taskId}/offers/{offerId}/accept")
  @Operation(
      operationId = "acceptTaskOffer",
      summary = "Accept task offer",
      description =
          "Atomically accepts one PENDING offer, creates a Deal and Chat. Requires Idempotency-Key.")
  public DealResponse acceptOffer(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestHeader(ApiConstants.IDEMPOTENCY_KEY_HEADER) String idempotencyKey,
      @Parameter(description = "Task publicId.") @PathVariable String taskId,
      @Parameter(description = "Task offer publicId.") @PathVariable String offerId) {
    return marketplaceService.acceptOffer(userDetails, idempotencyKey, taskId, offerId);
  }

  @PostMapping("/{taskId}/offers/{offerId}/cancel")
  @Operation(
      operationId = "cancelTaskOffer",
      summary = "Cancel current performer's pending offer",
      description =
          "Cancels the authenticated performer's PENDING offer. Requires Idempotency-Key.")
  public TaskOfferResponse cancelOffer(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestHeader(ApiConstants.IDEMPOTENCY_KEY_HEADER) String idempotencyKey,
      @Parameter(description = "Task publicId.") @PathVariable String taskId,
      @Parameter(description = "Task offer publicId.") @PathVariable String offerId) {
    return marketplaceService.cancelOffer(userDetails, idempotencyKey, taskId, offerId);
  }

  @PostMapping("/{taskId}/attachments")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
      operationId = "createTaskAttachmentUploadUrl",
      summary = "Create task image upload URL",
      description = "Creates public TASK_IMAGE metadata for a task owned by the current customer.")
  public TaskAttachmentUploadResponse addTaskAttachment(
      @AuthenticationPrincipal UserDetails userDetails,
      @Parameter(description = "Task publicId.") @PathVariable String taskId,
      @Valid @RequestBody AttachmentMetadataRequest request) {
    return marketplaceService.addTaskAttachment(userDetails, taskId, request);
  }

  @GetMapping("/{taskId}/attachments")
  @Operation(
      operationId = "listTaskAttachments",
      summary = "List public task images",
      description = "Returns safe image metadata for a published task.")
  public java.util.List<TaskAttachmentResponse> taskAttachments(
      @Parameter(description = "Task publicId.") @PathVariable String taskId) {
    return marketplaceService.taskAttachments(taskId);
  }

  @GetMapping("/{taskId}/attachments/{attachmentId}/download-url")
  @Operation(
      operationId = "createTaskAttachmentDownloadUrl",
      summary = "Create public task image download URL",
      description = "Issues a short-lived download URL for a published task image.")
  public AttachmentDownloadUrlResponse taskAttachmentDownloadUrl(
      @Parameter(description = "Task publicId.") @PathVariable String taskId,
      @Parameter(description = "Attachment publicId.") @PathVariable String attachmentId) {
    return marketplaceService.taskAttachmentDownloadUrl(taskId, attachmentId);
  }
}
