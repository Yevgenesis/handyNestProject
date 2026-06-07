package com.handynest.payment;

import com.handynest.common.api.ApiConstants;
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
public class PaymentApiV1Controller {

    private static final String WEBHOOK_TOKEN_HEADER = "X-HandyNest-Webhook-Token";

    private final PaymentService paymentService;

    @PostMapping("/deals/{dealId}/payments")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentTransactionResponse createPayment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String dealId,
            @RequestHeader(ApiConstants.IDEMPOTENCY_KEY_HEADER) String idempotencyKey,
            @Valid @RequestBody PaymentCreateRequest request
    ) {
        return paymentService.createPayment(userDetails, dealId, idempotencyKey, request);
    }

    @GetMapping("/deals/{dealId}/payments")
    public List<PaymentTransactionResponse> dealPayments(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String dealId
    ) {
        return paymentService.dealPayments(userDetails, dealId);
    }

    @PostMapping("/payments/{paymentId}/authorize")
    public PaymentTransactionResponse authorizePayment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String paymentId,
            @RequestHeader(ApiConstants.IDEMPOTENCY_KEY_HEADER) String idempotencyKey,
            @Valid @RequestBody(required = false) PaymentTransitionRequest request
    ) {
        return paymentService.authorizePayment(userDetails, paymentId, idempotencyKey, request);
    }

    @PostMapping("/payments/{paymentId}/hold")
    public PaymentTransactionResponse holdPayment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String paymentId,
            @RequestHeader(ApiConstants.IDEMPOTENCY_KEY_HEADER) String idempotencyKey,
            @Valid @RequestBody(required = false) PaymentTransitionRequest request
    ) {
        return paymentService.holdPayment(userDetails, paymentId, idempotencyKey, request);
    }

    @PostMapping("/payments/{paymentId}/release")
    public PaymentTransactionResponse releasePayment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String paymentId,
            @RequestHeader(ApiConstants.IDEMPOTENCY_KEY_HEADER) String idempotencyKey,
            @Valid @RequestBody(required = false) PaymentTransitionRequest request
    ) {
        return paymentService.releasePayment(userDetails, paymentId, idempotencyKey, request);
    }

    @PostMapping("/payments/{paymentId}/refund")
    public PaymentTransactionResponse refundPayment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String paymentId,
            @RequestHeader(ApiConstants.IDEMPOTENCY_KEY_HEADER) String idempotencyKey,
            @Valid @RequestBody(required = false) PaymentTransitionRequest request
    ) {
        return paymentService.refundPayment(userDetails, paymentId, idempotencyKey, request);
    }

    @PostMapping("/payments/{paymentId}/fail")
    public PaymentTransactionResponse failPayment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String paymentId,
            @RequestHeader(ApiConstants.IDEMPOTENCY_KEY_HEADER) String idempotencyKey,
            @Valid @RequestBody(required = false) PaymentTransitionRequest request
    ) {
        return paymentService.failPayment(userDetails, paymentId, idempotencyKey, request);
    }

    @PostMapping("/payments/{paymentId}/cancel")
    public PaymentTransactionResponse cancelPayment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String paymentId,
            @RequestHeader(ApiConstants.IDEMPOTENCY_KEY_HEADER) String idempotencyKey,
            @Valid @RequestBody(required = false) PaymentTransitionRequest request
    ) {
        return paymentService.cancelPayment(userDetails, paymentId, idempotencyKey, request);
    }

    @PostMapping("/payments/webhooks/{provider}")
    public PaymentTransactionResponse applyWebhook(
            @PathVariable String provider,
            @RequestHeader(ApiConstants.IDEMPOTENCY_KEY_HEADER) String idempotencyKey,
            @RequestHeader(WEBHOOK_TOKEN_HEADER) String webhookToken,
            @Valid @RequestBody PaymentWebhookRequest request
    ) {
        return paymentService.applyWebhook(provider, idempotencyKey, webhookToken, request);
    }
}
