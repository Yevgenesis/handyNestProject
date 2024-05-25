package codezilla.handynestproject.dto.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TaskRequestDto(

        @NotBlank @Size(min = 3, max = 50)
        String title,
        @NotBlank @Size(min = 8, max = 200)
        String description,
        Double price,
        @NotBlank @Size(min = 2, max = 50)
        String street,
        String city,
        String zip,
        String country,
        Long workingTimeId,
        @NotBlank
        Long userId,
        @NotBlank
        Long categoryId,
        @NotBlank
        boolean isPublish
) {
}
