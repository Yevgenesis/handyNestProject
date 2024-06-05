package codezilla.handynestproject.dto.task;//package codezilla.hendynestproject.model.entity;

import codezilla.handynestproject.dto.address.AddressDto;
import codezilla.handynestproject.dto.category.CategoryTitleDto;
import codezilla.handynestproject.dto.performer.PerformerNestedResponseDto;
import codezilla.handynestproject.dto.user.UserNestedResponseDto;
import codezilla.handynestproject.model.entity.WorkingTime;
import codezilla.handynestproject.model.enums.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description = "Task ID")
    private Long id;
    @Schema(description = "Task title")
    private String title;
    @Schema(description = "Task description")
    private String description;
    @Schema(description = "Task price")
    private Double price;
    @Schema(description = "Task address")
    private AddressDto address;
    @Schema(description = "Task status")
    private TaskStatus taskStatus;
    @Schema(description = "Task working time")
    private WorkingTime workingTime;
    @Schema(description = "Task category")
    private CategoryTitleDto category;
    @Schema(description = "Task creator")
    private UserNestedResponseDto user;
    @Schema(description = "Task performer")
    private PerformerNestedResponseDto performer;
    @Schema(description = "Flag indicating if task is published")
    @Builder.Default
    private boolean isPublish = true;
    @Schema(description = "Timestamp of task creation")
    private Timestamp createdOn;
    @Schema(description = "Timestamp of last task update")
    private Timestamp updatedOn;
}
