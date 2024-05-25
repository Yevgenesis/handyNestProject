package codezilla.handynestproject.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserRequestDto(
        @NotBlank
        String firstName,
        @NotBlank
        String lastName,
        @NotBlank @Email
        String email,
        @NotBlank @Pattern(regexp = PASSWORD_REGEX, message = BAD_PASSWORD_MESSAGE)
        String password,
        @NotBlank @Pattern(regexp = PASSWORD_REGEX, message = BAD_PASSWORD_MESSAGE)
        String passwordConfirmation
) {
    private static final String PASSWORD_REGEX = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,20}$";
    private static final String BAD_PASSWORD_MESSAGE = "Password must be min 8 symbols, contains lower case," +
            "digit and specials symbols (@#$%^&+=) ";

    // проверка совпадения password и passwordConfirmation
    public boolean isPasswordsMatch() {
        return password.equals(passwordConfirmation);
    }
}
