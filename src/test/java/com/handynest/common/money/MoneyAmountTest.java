package com.handynest.common.money;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MoneyAmountTest {

    @Test
    void defaultsCurrencyToKzt() {
        MoneyAmount amount = new MoneyAmount(new BigDecimal("1000.00"), null);

        assertEquals(new BigDecimal("1000.00"), amount.amount());
        assertEquals(CurrencyCode.KZT, amount.currency());
    }

    @Test
    void rejectsNullAmount() {
        assertThrows(NullPointerException.class, () -> new MoneyAmount(null, CurrencyCode.KZT));
    }
}
