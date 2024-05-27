package codezilla.handynestproject.dto.task;

import codezilla.handynestproject.dto.address.AddressDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TaskRequestDto(

        @NotBlank @Size(min = 3, max = 50)
        String title,
        @NotBlank @Size(min = 8, max = 200)
        String description,
        Double price,
        AddressDto address,
        @NotNull
        Long workingTimeId,
        @NotNull
        Long userId,
        @NotNull
        Long categoryId,
        @NotNull
        boolean isPublish
) {
}
