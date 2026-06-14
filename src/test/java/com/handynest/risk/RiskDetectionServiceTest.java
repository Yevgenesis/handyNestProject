package com.handynest.risk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RiskDetectionServiceTest {

  private final RiskDetectionService service = new RiskDetectionService();

  @Test
  void detectsUzbekPhoneNumbersWithCommonObfuscation() {
    assertRisk("Позвоните +998 (90) 123-45-67", RiskType.PHONE_SHARED);
    assertRisk("raqamim 998 91 555 44 33", RiskType.PHONE_SHARED);
  }

  @Test
  void detectsMessengersAndUzbekBypassPhrases() {
    assertRisk("Напишите в Telegram", RiskType.EXTERNAL_MESSENGER);
    assertRisk("whatsappda yozing", RiskType.EXTERNAL_MESSENGER);
    assertRisk("shaxsiyga yozing", RiskType.EXTERNAL_MESSENGER);
    assertRisk("instagram: handy.master", RiskType.EXTERNAL_MESSENGER);
  }

  @Test
  void prioritizesExternalPaymentSignals() {
    assertRisk("platformadan tashqari Payme orqali to'lang", RiskType.PAYMENT_OUTSIDE_PLATFORM);
    assertRisk("Оплата вне платформы, переведи на карту", RiskType.PAYMENT_OUTSIDE_PLATFORM);
  }

  private void assertRisk(String text, RiskType expectedType) {
    var result = service.detect(text);

    assertTrue(result.isPresent());
    assertEquals(expectedType, result.orElseThrow().riskType());
  }
}
