package com.handynest.payment;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentTransactionResponse(
        String publicId,
        String dealId,
        String taskId,
        String customerId,
        String performerId,
        BigDecimal amount,
        BigDecimal platformFeeAmount,
        BigDecimal performerAmount,
        String currency,
        String status,
        String paymentMode,
        String pspProvider,
        String pspPaymentId,
        String pspReference,
        String failureReason,
        Instant authorizedAt,
        Instant heldAt,
        Instant releasedAt,
        Instant refundedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
