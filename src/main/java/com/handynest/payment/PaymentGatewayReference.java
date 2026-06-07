package com.handynest.payment;

public record PaymentGatewayReference(
        String provider,
        String pspPaymentId,
        String pspReference
) {
}
