package com.handynest.payment;

import jakarta.validation.constraints.Size;

public record PaymentTransitionRequest(
        @Size(max = 1000)
        String failureReason
) {
}
