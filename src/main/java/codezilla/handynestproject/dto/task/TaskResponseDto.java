package codezilla.handynestproject.dto.task;//package codezilla.hendynestproject.model.entity;

import codezilla.handynestproject.model.entity.Address;
import codezilla.handynestproject.model.entity.Category;
import codezilla.handynestproject.model.entity.User;
import codezilla.handynestproject.model.entity.WorkingTime;
import codezilla.handynestproject.model.enums.TaskStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

// ToDo проверить (Тестовый DTO)

@Data
public class TaskResponseDto {
    private Long id;
    private String title;
    private String description;
    private Double price;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Address address;
    private TaskStatus taskStatus;
    private WorkingTime workingTime;
    private Category category;
    private User user;
    private boolean isPublish;
   }
