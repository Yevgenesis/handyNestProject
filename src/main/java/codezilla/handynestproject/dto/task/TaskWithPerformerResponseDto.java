package codezilla.handynestproject.dto.task;//package codezilla.hendynestproject.model.entity;

import codezilla.handynestproject.model.entity.Address;
import codezilla.handynestproject.model.entity.Category;
import codezilla.handynestproject.model.entity.Performer;
import codezilla.handynestproject.model.entity.User;
import codezilla.handynestproject.model.entity.WorkingTime;
import codezilla.handynestproject.model.enums.TaskStatus;
import lombok.Data;

// ToDo проверить (Тестовый DTO)

@Data
public class TaskWithPerformerResponseDto {
    private Long id;
    private String title;
    private String description;
    private Double price;
    private Address address;
    private TaskStatus taskStatus;
    private WorkingTime workingTime;
    private Category category;
    private User user;
    private Performer performer;
    private boolean isPublish;
   }
