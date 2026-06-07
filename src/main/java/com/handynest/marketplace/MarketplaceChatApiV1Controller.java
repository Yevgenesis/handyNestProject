package com.handynest.marketplace;

import com.handynest.common.api.PageResponse;
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
public class MarketplaceChatApiV1Controller {

    private final MarketplaceService marketplaceService;

    @GetMapping
    public List<DealChatResponse> myChats(@AuthenticationPrincipal UserDetails userDetails) {
        return marketplaceService.myChats(userDetails);
    }

    @GetMapping("/{chatId}")
    public DealChatResponse findChat(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String chatId
    ) {
        return marketplaceService.findChat(userDetails, chatId);
    }

    @GetMapping("/{chatId}/messages")
    public PageResponse<ChatMessageResponse> listMessages(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String chatId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return marketplaceService.listChatMessages(userDetails, chatId, page, size);
    }

    @PostMapping("/{chatId}/messages")
    public ChatMessageResponse sendMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String chatId,
            @Valid @RequestBody ChatMessageRequest request
    ) {
        return marketplaceService.sendChatMessage(userDetails, chatId, request);
    }

    @PostMapping("/{chatId}/mark-read")
    public ResponseEntity<Void> markRead(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String chatId
    ) {
        marketplaceService.markChatRead(userDetails, chatId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{chatId}/submit-work")
    public DealResponse submitWork(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @PathVariable String chatId,
            @Valid @RequestBody(required = false) WorkSubmissionRequest request
    ) {
        return marketplaceService.submitWork(
                userDetails,
                idempotencyKey,
                chatId,
                request == null ? new WorkSubmissionRequest(null) : request
        );
    }

    @PostMapping("/{chatId}/accept-work")
    public DealResponse acceptWork(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @PathVariable String chatId,
            @Valid @RequestBody(required = false) WorkAcceptanceRequest request
    ) {
        return marketplaceService.acceptWork(
                userDetails,
                idempotencyKey,
                chatId,
                request == null ? new WorkAcceptanceRequest(null) : request
        );
    }

    @PostMapping("/{chatId}/request-revision")
    public DealResponse requestRevision(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @PathVariable String chatId,
            @Valid @RequestBody RevisionRequest request
    ) {
        return marketplaceService.requestRevision(userDetails, idempotencyKey, chatId, request);
    }

    @PostMapping("/{chatId}/open-dispute")
    public DisputeCaseResponse openDispute(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @PathVariable String chatId,
            @Valid @RequestBody OpenDisputeRequest request
    ) {
        return marketplaceService.openDispute(userDetails, idempotencyKey, chatId, request);
    }
}
