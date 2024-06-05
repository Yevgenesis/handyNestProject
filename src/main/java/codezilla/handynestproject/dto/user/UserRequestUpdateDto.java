package codezilla.handynestproject.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserRequestUpdateDto(

        @Schema(description = "User ID")
        @NotNull
        Long id,
        @Schema(description = "User first name")
        @NotBlank
        String firstName,
        @Schema(description = "User last name")
        @NotBlank
        String lastName,
        @Schema(description = "User logo")
        String logo
) {
}