package codezilla.handynestproject.dto.task;

import codezilla.handynestproject.dto.address.AddressDto;
import codezilla.handynestproject.model.entity.Address;
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

    @NotNull(message = "Task ID can not be empty")
    private Long id;
    @NotBlank(message = "Title can not be empty")
    @Size(min = 3, max = 50,message = "Title can be empty min = 3 and max = 50 symbols")
    private String title;
    @NotBlank(message = "Description can not be empty")
    @Size(min = 10, max = 150,message = "Title can be empty min = 10 and max = 150 symbols")
    private String description;
    @NotNull(message = "Price can not be null")
    private Double price;
    @NotNull(message = "Address can not be null")
    private AddressDto addressDto;//TODO возможно переделать адрес построчно
    @NotNull(message = "Address can not be null")
    private Long workingTimeId;


}
