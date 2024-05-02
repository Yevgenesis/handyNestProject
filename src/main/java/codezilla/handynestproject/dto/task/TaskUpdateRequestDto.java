package codezilla.handynestproject.dto.task;

import codezilla.handynestproject.model.entity.Address;
import codezilla.handynestproject.model.entity.Category;
import codezilla.handynestproject.model.entity.User;
import codezilla.handynestproject.model.entity.enums.TaskStatus;
import lombok.Data;

@Data
public class TaskUpdateRequestDto {
    private Long id;
    private String title;
    private String description;
    private Double price;
    private Address address;
    private TaskStatus taskStatus;
    private Long workingTimeId;
    private Category category;
    private User user;
}
