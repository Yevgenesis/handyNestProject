package codezilla.handynestproject.dto.feedback;

import codezilla.handynestproject.dto.user.UserNestedResponseDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FeedbackResponseDto {

    @Schema(description = "Feedback ID")
    private Long id;
    @Schema(description = "Feedback sender")
    private UserNestedResponseDto sender;
    @Schema(description = "Feedback text")
    private String text;
    @Schema(description = "Feedback grade")
    private Long grade;
    @Schema(description = "Task ID")
    private Long taskId;
    @Schema(description = "Timestamp of feedback creation")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy HH:mm:ss")
    private Timestamp createdOn;
}