package com.handynest.marketplace;

import com.handynest.common.api.ApiConstants;
import com.handynest.common.api.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/v1/chats")
@RequiredArgsConstructor
@Tag(name = "Chats", description = "Deal chat messages and work lifecycle actions.")
public class MarketplaceChatApiV1Controller {

  private final MarketplaceService marketplaceService;

  @GetMapping
  @Operation(
      operationId = "listMyChats",
      summary = "List my chats",
      description = "Returns deal chats for the authenticated customer or performer.")
  public List<DealChatResponse> myChats(@AuthenticationPrincipal UserDetails userDetails) {
    return marketplaceService.myChats(userDetails);
  }

  @GetMapping("/{chatId}")
  @Operation(
      operationId = "getChat",
      summary = "Get chat",
      description = "Returns one deal chat by publicId.")
  public DealChatResponse findChat(
      @AuthenticationPrincipal UserDetails userDetails,
      @Parameter(description = "Chat publicId.") @PathVariable String chatId) {
    return marketplaceService.findChat(userDetails, chatId);
  }

  @GetMapping("/{chatId}/messages")
  @Operation(
      operationId = "listChatMessages",
      summary = "List chat messages",
      description = "Returns paginated messages for a deal chat publicId.")
  public PageResponse<ChatMessageResponse> listMessages(
      @AuthenticationPrincipal UserDetails userDetails,
      @Parameter(description = "Chat publicId.") @PathVariable String chatId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return marketplaceService.listChatMessages(userDetails, chatId, page, size);
  }

  @GetMapping("/{chatId}/messages/timeline")
  @Operation(
      operationId = "getChatMessageTimeline",
      summary = "Get chat message timeline",
      description =
          "Returns the latest messages, messages before a cursor, or messages after a cursor. "
              + "Cursors are message publicIds; before and after cannot be combined.")
  public ChatMessageTimelineResponse timeline(
      @AuthenticationPrincipal UserDetails userDetails,
      @Parameter(description = "Chat publicId.") @PathVariable String chatId,
      @Parameter(description = "Load messages older than this message publicId.")
          @RequestParam(required = false)
          String before,
      @Parameter(description = "Load messages newer than this message publicId.")
          @RequestParam(required = false)
          String after,
      @RequestParam(defaultValue = "40") int limit) {
    return marketplaceService.chatMessageTimeline(userDetails, chatId, before, after, limit);
  }

  @PostMapping("/{chatId}/messages")
  @Operation(
      operationId = "sendChatMessage",
      summary = "Send chat message",
      description = "Sends a text message in an active deal chat.")
  public ChatMessageResponse sendMessage(
      @AuthenticationPrincipal UserDetails userDetails,
      @Parameter(description = "Chat publicId.") @PathVariable String chatId,
      @Valid @RequestBody ChatMessageRequest request) {
    return marketplaceService.sendChatMessage(userDetails, chatId, request);
  }

  @PostMapping("/{chatId}/mark-read")
  @Operation(
      operationId = "markChatRead",
      summary = "Mark chat as read",
      description = "Marks unread messages as read for the authenticated chat participant.")
  public ResponseEntity<Void> markRead(
      @AuthenticationPrincipal UserDetails userDetails,
      @Parameter(description = "Chat publicId.") @PathVariable String chatId) {
    marketplaceService.markChatRead(userDetails, chatId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{chatId}/submit-work")
  @Operation(
      operationId = "submitDealWork",
      summary = "Submit work",
      description = "Performer submits work for customer acceptance. Requires Idempotency-Key.")
  public DealResponse submitWork(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestHeader(ApiConstants.IDEMPOTENCY_KEY_HEADER) String idempotencyKey,
      @Parameter(description = "Chat publicId.") @PathVariable String chatId,
      @Valid @RequestBody(required = false) WorkSubmissionRequest request) {
    return marketplaceService.submitWork(
        userDetails,
        idempotencyKey,
        chatId,
        request == null ? new WorkSubmissionRequest(null) : request);
  }

  @PostMapping("/{chatId}/accept-work")
  @Operation(
      operationId = "acceptDealWork",
      summary = "Accept work",
      description =
          "Customer accepts submitted work and completes the deal. Requires Idempotency-Key.")
  public DealResponse acceptWork(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestHeader(ApiConstants.IDEMPOTENCY_KEY_HEADER) String idempotencyKey,
      @Parameter(description = "Chat publicId.") @PathVariable String chatId,
      @Valid @RequestBody(required = false) WorkAcceptanceRequest request) {
    return marketplaceService.acceptWork(
        userDetails,
        idempotencyKey,
        chatId,
        request == null ? new WorkAcceptanceRequest(null) : request);
  }

  @PostMapping("/{chatId}/request-revision")
  @Operation(
      operationId = "requestDealRevision",
      summary = "Request revision",
      description = "Customer requests revision after work submission. Requires Idempotency-Key.")
  public DealResponse requestRevision(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestHeader(ApiConstants.IDEMPOTENCY_KEY_HEADER) String idempotencyKey,
      @Parameter(description = "Chat publicId.") @PathVariable String chatId,
      @Valid @RequestBody RevisionRequest request) {
    return marketplaceService.requestRevision(userDetails, idempotencyKey, chatId, request);
  }

  @PostMapping("/{chatId}/open-dispute")
  @Operation(
      operationId = "openDealDispute",
      summary = "Open dispute",
      description = "Opens a dispute for a deal chat. Requires Idempotency-Key.")
  public DisputeCaseResponse openDispute(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestHeader(ApiConstants.IDEMPOTENCY_KEY_HEADER) String idempotencyKey,
      @Parameter(description = "Chat publicId.") @PathVariable String chatId,
      @Valid @RequestBody OpenDisputeRequest request) {
    return marketplaceService.openDispute(userDetails, idempotencyKey, chatId, request);
  }
}
