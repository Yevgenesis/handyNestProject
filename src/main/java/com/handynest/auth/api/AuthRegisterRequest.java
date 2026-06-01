package com.handynest.auth.api;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AuthRegisterRequest(
        @NotBlank @Size(max = 50) String firstName,
        @NotBlank @Size(max = 50) String lastName,
        @NotBlank @Email @Size(max = 50) String email,
        @NotBlank @Pattern(regexp = PASSWORD_REGEX, message = BAD_PASSWORD_MESSAGE) String password,
        @NotBlank @Pattern(regexp = PASSWORD_REGEX, message = BAD_PASSWORD_MESSAGE) String passwordConfirmation
) {
    private static final String PASSWORD_REGEX =
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,20}$";
    private static final String BAD_PASSWORD_MESSAGE =
            "Password must be min 8 symbols, contains lower case, digit and specials symbols (@#$%^&+=)";

    @AssertTrue(message = "Password confirmation must match password")
    public boolean isPasswordsMatch() {
        return password != null && password.equals(passwordConfirmation);
    }
}
