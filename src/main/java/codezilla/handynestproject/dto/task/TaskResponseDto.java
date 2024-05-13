package codezilla.handynestproject.dto.task;//package codezilla.hendynestproject.model.entity;

import codezilla.handynestproject.dto.address.AddressDto;
import codezilla.handynestproject.dto.category.CategoryTitleDto;
import codezilla.handynestproject.dto.performer.PerformerResponseDto;
import codezilla.handynestproject.dto.user.UserResponseDto;
import codezilla.handynestproject.model.entity.Address;
import codezilla.handynestproject.model.entity.WorkingTime;
import codezilla.handynestproject.model.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// ToDo проверить (Тестовый DTO)
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class TaskResponseDto {
    private Long id;
    private String title;
    private String description;
    private Double price;
    private AddressDto address;
    private TaskStatus taskStatus;
    private WorkingTime workingTime;
    private CategoryTitleDto category;
    private UserResponseDto user;
    private PerformerResponseDto performer;
    private boolean isPublish;
   }
