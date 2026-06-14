package com.handynest.testsupport;

import java.util.List;
import java.util.Map;

public final class AuthTestPayloads {

  private static final String DOCUMENT_VERSION = "1.0";

  private AuthTestPayloads() {}

  public static Map<String, Object> registrationPayload(
      String firstName, String lastName, String email, String password) {
    return Map.of(
        "firstName", firstName,
        "lastName", lastName,
        "email", email,
        "password", password,
        "passwordConfirmation", password,
        "consents", allConsents());
  }

  public static List<Map<String, String>> allConsents() {
    return List.of(
        consent("TERMS_OF_SERVICE"),
        consent("PRIVACY_POLICY"),
        consent("PERSONAL_DATA_PROCESSING"),
        consent("PERFORMER_RULES"),
        consent("CUSTOMER_RULES"),
        consent("PROHIBITED_SERVICES_POLICY"),
        consent("PAYMENT_POLICY"));
  }

  private static Map<String, String> consent(String type) {
    return Map.of("type", type, "documentVersion", DOCUMENT_VERSION);
  }
}
