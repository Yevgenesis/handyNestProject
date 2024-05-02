package codezilla.handynestproject.dto.task;//package codezilla.hendynestproject.model.entity;

import codezilla.handynestproject.model.entity.Address;
import codezilla.handynestproject.model.entity.Category;
import codezilla.handynestproject.model.entity.User;
import codezilla.handynestproject.model.enums.TaskStatus;
import lombok.Data;

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
    private boolean isPublish;
   }
