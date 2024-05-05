package codezilla.handynestproject.dto.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TaskRequestDto(
        @NotBlank @Size(min = 3, max = 50)
        String title,
        @NotBlank @Size(min = 8, max = 200)
        String description,
        Double price,
        String country,
        String city,
        String street,
        String zip,
        Long workingTimeId,
        Long userId,
        Long categoryId,
        boolean isPublish



) {
}
