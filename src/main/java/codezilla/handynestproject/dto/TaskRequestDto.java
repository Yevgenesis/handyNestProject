package codezilla.handynestproject.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record TaskRequestDto(
        @NotBlank @Size(min = 3, max = 50) String title,
        @NotBlank @Size(min = 8, max = 200) String description,
        Double price,
        List<Double> location,
        List<Integer> workingTime,
        @NotNull Integer categoryId,
        @NotBlank Long userId
) {
}
