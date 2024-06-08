package codezilla.handynestproject.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserRequestDto(

        @Schema(description = "User first name", example = "John")
        @NotBlank
        String firstName,
        @Schema(description = "User last name", example = "Doe")
        @NotBlank
        String lastName,
        @Schema(description = "User email", example = "john.doe@example.com")
        @NotBlank @Email
        String email,
        @Schema(description = "User password")
        @NotBlank @Pattern(regexp = PASSWORD_REGEX, message = BAD_PASSWORD_MESSAGE)
        String password,
        @Schema(description = "Password confirmation")
        @NotBlank @Pattern(regexp = PASSWORD_REGEX, message = BAD_PASSWORD_MESSAGE)
        String passwordConfirmation
) {
    private static final String PASSWORD_REGEX = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,20}$";
    private static final String BAD_PASSWORD_MESSAGE = "Password must be min 8 symbols, contains lower case," +
            "digit and specials symbols (@#$%^&+=) ";

    // checking the match of password and passwordConfirmation
    public boolean isPasswordsMatch() {
        return password.equals(passwordConfirmation);
    }
}
