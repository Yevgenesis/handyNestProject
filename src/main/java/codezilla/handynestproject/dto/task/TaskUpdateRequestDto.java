package codezilla.handynestproject.dto.task;

import codezilla.handynestproject.model.entity.Address;
import lombok.Data;

@Data
public class TaskUpdateRequestDto {

    private Long id;
    private String title;
    private String description;
    private Double price;
    private Address address;//TODO возможно переделать адрес построчно
    private Long workingTimeId;


}
