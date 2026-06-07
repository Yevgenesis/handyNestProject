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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class MarketplaceAttachmentApiV1Controller {

    private final MarketplaceService marketplaceService;

    @PostMapping("/chats/{chatId}/attachments")
    @ResponseStatus(HttpStatus.CREATED)
    public AttachmentUploadResponse addChatAttachment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String chatId,
            @Valid @RequestBody AttachmentMetadataRequest request
    ) {
        return marketplaceService.addChatAttachment(userDetails, chatId, request);
    }

    @GetMapping("/chats/{chatId}/attachments")
    public List<AttachmentResponse> chatAttachments(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String chatId
    ) {
        return marketplaceService.chatAttachments(userDetails, chatId);
    }

    @PostMapping("/disputes/{disputeId}/attachments")
    @ResponseStatus(HttpStatus.CREATED)
    public AttachmentUploadResponse addDisputeAttachment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String disputeId,
            @Valid @RequestBody AttachmentMetadataRequest request
    ) {
        return marketplaceService.addDisputeAttachment(userDetails, disputeId, request);
    }

    @GetMapping("/disputes/{disputeId}/attachments")
    public List<AttachmentResponse> disputeAttachments(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String disputeId
    ) {
        return marketplaceService.disputeAttachments(userDetails, disputeId);
    }

    @PostMapping("/attachments/{attachmentId}/download-url")
    public AttachmentDownloadUrlResponse attachmentDownloadUrl(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String attachmentId
    ) {
        return marketplaceService.attachmentDownloadUrl(userDetails, attachmentId);
    }
}
