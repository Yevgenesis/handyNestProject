package codezilla.handynestproject.dto.task;

import codezilla.handynestproject.dto.address.AddressDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TaskUpdateRequestDto {

    @Schema(description = "Task ID")
    @NotNull(message = "Task ID can not be empty")
    private Long id;
    @Schema(description = "Task title")
    @NotBlank(message = "Title can not be empty")
    @Size(min = 3, max = 50, message = "Title can be empty min = 3 and max = 50 symbols")
    private String title;
    @Schema(description = "Task description")
    @NotBlank(message = "Description can not be empty")
    @Size(min = 10, max = 150, message = "Title can be empty min = 10 and max = 150 symbols")
    private String description;
    @Schema(description = "Task price")
    @NotNull(message = "Price can not be null")
    private Double price;
    @Schema(description = "Task address")
    @NotNull(message = "Address can not be null")
    private AddressDto addressDto;
    @Schema(description = "Task working time ID")
    @NotNull(message = "Address can not be null")
    private Long workingTimeId;
}
