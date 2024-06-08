package codezilla.handynestproject.dto.task;

import codezilla.handynestproject.dto.address.AddressDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TaskRequestDto(

        @Schema(description = "Task title", example = "Need help with my garden")
        @NotBlank @Size(min = 3, max = 50)
        String title,
        @Schema(description = "Task description", example = "I need someone to mow my lawn and trim the bushes")
        @NotBlank @Size(min = 8, max = 200)
        String description,
        @Schema(description = "Task price", example = "50.0")
        Double price,
        @Schema(description = "Task address")
        AddressDto address,
        @Schema(description = "Working time ID")
        @NotNull
        Long workingTimeId,
        @Schema(description = "User ID")
        @NotNull
        Long userId,
        @Schema(description = "Category ID")
        @NotNull
        Long categoryId,
        @Schema(description = "Flag indicating if task is published", example = "true")
        @NotNull
        boolean isPublish
) {}
