package codezilla.handynestproject.dto.task;//package codezilla.hendynestproject.model.entity;

import codezilla.handynestproject.dto.address.AddressDto;
import codezilla.handynestproject.dto.category.CategoryTitleDto;
import codezilla.handynestproject.dto.performer.PerformerNestedResponseDto;
import codezilla.handynestproject.dto.user.UserNestedResponseDto;
import codezilla.handynestproject.model.entity.WorkingTime;
import codezilla.handynestproject.model.enums.TaskStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

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
    private UserNestedResponseDto user;
    private PerformerNestedResponseDto performer;
    @Builder.Default
    private boolean isPublish = true;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy HH:mm:ss")
    private Timestamp createdOn;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy HH:mm:ss")
    private Timestamp updatedOn;
}
