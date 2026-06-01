package com.handynest.common.money;

import java.math.BigDecimal;
import java.util.Objects;

public record MoneyAmount(
        BigDecimal amount,
        CurrencyCode currency
) {

    public MoneyAmount {
        amount = Objects.requireNonNull(amount, "amount must not be null");
        if (currency == null) {
            currency = CurrencyCode.KZT;
        }
    }

    public static MoneyAmount kzt(BigDecimal amount) {
        return new MoneyAmount(amount, CurrencyCode.KZT);
    }
}
