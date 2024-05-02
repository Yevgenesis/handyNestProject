package codezilla.handynestproject.dto.task;//package codezilla.hendynestproject.model.entity;

import codezilla.handynestproject.model.entity.Address;
import codezilla.handynestproject.model.entity.Category;
import codezilla.handynestproject.model.entity.Feedback;
import codezilla.handynestproject.model.entity.User;
import codezilla.handynestproject.model.entity.WorkingTime;
import codezilla.handynestproject.model.entity.enums.TaskStatus;
import lombok.Data;

import java.util.Set;

// ToDo проверить (Тестовый DTO)
@Data
public class TaskResponseDto {
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
