package codezilla.handynestproject.dto;

import codezilla.handynestproject.model.entity.Address;
import codezilla.handynestproject.model.entity.WorkingTime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record TaskRequestDto(
        @NotBlank @Size(min = 3, max = 50) String title,
        @NotBlank @Size(min = 8, max = 200) String description,
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
