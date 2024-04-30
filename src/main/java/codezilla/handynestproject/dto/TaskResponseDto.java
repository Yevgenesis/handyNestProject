package codezilla.handynestproject.dto;//package codezilla.hendynestproject.model.entity;

import codezilla.handynestproject.model.entity.Category;
import codezilla.handynestproject.model.entity.Feedback;
import codezilla.handynestproject.model.entity.User;
import codezilla.handynestproject.model.entity.WorkingTime;
import lombok.Data;

import java.util.Set;

// ToDo исправить (Тестовый DTO)
@Data
public class TaskResponseDto {
    private Long id;
    private String title;
    private String description;
    private Double price;
    private String location;
    private String state;
    private WorkingTime workingTime;
    private Category category;
    private User user;
    private Set<Feedback> feedbacks;
}
