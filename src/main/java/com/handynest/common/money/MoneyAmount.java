package com.handynest.common.money;

import java.math.BigDecimal;
import java.util.Objects;

public record MoneyAmount(BigDecimal amount, CurrencyCode currency) {

  public MoneyAmount(BigDecimal amount, CurrencyCode currency) {
    this.amount = Objects.requireNonNull(amount, "amount must not be null");
    this.currency = Objects.requireNonNullElse(currency, CurrencyCode.UZS);
  }

  public static MoneyAmount kzt(BigDecimal amount) {
    return new MoneyAmount(amount, CurrencyCode.KZT);
  }

  public static MoneyAmount uzs(BigDecimal amount) {
    return new MoneyAmount(amount, CurrencyCode.UZS);
  }
}
