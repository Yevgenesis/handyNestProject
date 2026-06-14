package com.handynest.payment;

import com.handynest.marketplace.PaymentMode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record PaymentCreateRequest(
    @NotNull @DecimalMin("0.01") BigDecimal amount,
    @Size(min = 3, max = 3) String currency,
    PaymentMode paymentMode) {}
