package com.handynest.auth.api;

import com.handynest.identity.ConsentAcceptanceRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public record AuthRegisterRequest(
    @NotBlank @Size(max = 50) String firstName,
    @NotBlank @Size(max = 50) String lastName,
    @NotBlank @Email @Size(max = 50) String email,
    @NotBlank @Pattern(regexp = PASSWORD_REGEX, message = BAD_PASSWORD_MESSAGE) String password,
    @NotBlank @Pattern(regexp = PASSWORD_REGEX, message = BAD_PASSWORD_MESSAGE)
        String passwordConfirmation,
    @NotEmpty List<@Valid ConsentAcceptanceRequest> consents) {
  private static final String PASSWORD_REGEX =
      "^(?=.*\\p{N})(?=.*\\p{Ll})(?=.*\\p{Lu})(?=.*[^\\p{L}\\p{N}\\s])(?=\\S+$).{8,20}$";
  private static final String BAD_PASSWORD_MESSAGE =
      "Password must contain 8-20 characters, upper and lower case letters, a digit and a special character";

  @AssertTrue(message = "Password confirmation must match password")
  public boolean isPasswordsMatch() {
    return password != null && password.equals(passwordConfirmation);
  }
}
