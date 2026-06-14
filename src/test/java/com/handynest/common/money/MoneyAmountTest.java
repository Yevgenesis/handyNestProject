package com.handynest.common.money;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MoneyAmountTest {

  @Test
  void defaultsCurrencyToUzs() {
    MoneyAmount amount = new MoneyAmount(new BigDecimal("1000.00"), null);

    assertEquals(new BigDecimal("1000.00"), amount.amount());
    assertEquals(CurrencyCode.UZS, amount.currency());
  }

  @Test
  void rejectsNullAmount() {
    assertThrows(NullPointerException.class, () -> new MoneyAmount(null, CurrencyCode.UZS));
  }
}
