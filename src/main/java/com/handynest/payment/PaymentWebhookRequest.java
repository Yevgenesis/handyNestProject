package com.handynest.payment;

import com.handynest.marketplace.PaymentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PaymentWebhookRequest(
    @NotBlank @Size(max = 120) String pspPaymentId,
    @Size(max = 160) String pspReference,
    @NotNull PaymentStatus status,
    @Size(max = 1000) String failureReason) {}
