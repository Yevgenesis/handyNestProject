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
@Tag(
    name = "Marketplace Files",
    description = "Chat and dispute attachment metadata with storage URLs.")
public class MarketplaceAttachmentApiV1Controller {

  private final MarketplaceService marketplaceService;

  @PostMapping("/chats/{chatId}/attachments")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
      operationId = "createChatAttachment",
      summary = "Create chat attachment metadata",
      description = "Creates attachment metadata and upload URL for a chat publicId.")
  public AttachmentUploadResponse addChatAttachment(
      @AuthenticationPrincipal UserDetails userDetails,
      @Parameter(description = "Chat publicId.") @PathVariable String chatId,
      @Valid @RequestBody AttachmentMetadataRequest request) {
    return marketplaceService.addChatAttachment(userDetails, chatId, request);
  }

  @GetMapping("/chats/{chatId}/attachments")
  @Operation(
      operationId = "listChatAttachments",
      summary = "List chat attachments",
      description = "Returns attachments visible to a chat participant.")
  public List<AttachmentResponse> chatAttachments(
      @AuthenticationPrincipal UserDetails userDetails,
      @Parameter(description = "Chat publicId.") @PathVariable String chatId) {
    return marketplaceService.chatAttachments(userDetails, chatId);
  }

  @PostMapping("/chats/{chatId}/attachments/{attachmentId}/complete")
  @Operation(
      operationId = "completeChatAttachment",
      summary = "Complete chat attachment upload",
      description =
          "Verifies the uploaded object and creates one visible attachment message. "
              + "Repeated completion returns the same message.")
  public ChatAttachmentCompleteResponse completeChatAttachment(
      @AuthenticationPrincipal UserDetails userDetails,
      @Parameter(description = "Chat publicId.") @PathVariable String chatId,
      @Parameter(description = "Attachment publicId.") @PathVariable String attachmentId) {
    return marketplaceService.completeChatAttachment(userDetails, chatId, attachmentId);
  }

  @PostMapping("/disputes/{disputeId}/attachments")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
      operationId = "createDisputeAttachment",
      summary = "Create dispute attachment metadata",
      description = "Creates evidence attachment metadata and upload URL for a dispute publicId.")
  public AttachmentUploadResponse addDisputeAttachment(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestHeader(ApiConstants.IDEMPOTENCY_KEY_HEADER) String idempotencyKey,
      @Parameter(description = "Dispute case publicId.") @PathVariable String disputeId,
      @Valid @RequestBody AttachmentMetadataRequest request) {
    return marketplaceService.addDisputeAttachment(userDetails, idempotencyKey, disputeId, request);
  }

  @GetMapping("/disputes/{disputeId}/attachments")
  @Operation(
      operationId = "listDisputeAttachments",
      summary = "List dispute attachments",
      description = "Returns dispute evidence visible to participants or admins.")
  public List<AttachmentResponse> disputeAttachments(
      @AuthenticationPrincipal UserDetails userDetails,
      @Parameter(description = "Dispute case publicId.") @PathVariable String disputeId) {
    return marketplaceService.disputeAttachments(userDetails, disputeId);
  }

  @PostMapping("/attachments/{attachmentId}/download-url")
  @Operation(
      operationId = "createAttachmentDownloadUrl",
      summary = "Create attachment download URL",
      description = "Issues a short-lived download URL for an attachment publicId.")
  public AttachmentDownloadUrlResponse attachmentDownloadUrl(
      @AuthenticationPrincipal UserDetails userDetails,
      @Parameter(description = "Attachment publicId.") @PathVariable String attachmentId) {
    return marketplaceService.attachmentDownloadUrl(userDetails, attachmentId);
  }
}
