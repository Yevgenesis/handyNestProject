package com.handynest.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.handynest.auth.api.AuthRegisterRequest;
import com.handynest.identity.ConsentAcceptanceRequest;
import com.handynest.identity.ConsentType;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class AuthRegisterRequestValidationTest {

  private static Validator validator;

  @BeforeAll
  static void createValidator() {
    validator = Validation.buildDefaultValidatorFactory().getValidator();
  }

  @Test
  void acceptsPasswordsWithLatinCyrillicAndUzbekLetters() {
    assertThat(violationsFor("Test121314#")).isZero();
    assertThat(violationsFor("Пароль1!A")).isZero();
    assertThat(violationsFor("O‘zbekiston1!")).isZero();
  }

  @Test
  void rejectsPasswordWithoutRequiredCharacterGroups() {
    assertThat(violationsFor("NoSpecial1")).isPositive();
    assertThat(violationsFor("nouppercase1!")).isPositive();
  }

  private int violationsFor(String password) {
    AuthRegisterRequest request =
        new AuthRegisterRequest(
            "Test",
            "User",
            "test@example.com",
            password,
            password,
            List.of(new ConsentAcceptanceRequest(ConsentType.TERMS_OF_SERVICE, "1.0")));
    return validator.validate(request).size();
  }
}
