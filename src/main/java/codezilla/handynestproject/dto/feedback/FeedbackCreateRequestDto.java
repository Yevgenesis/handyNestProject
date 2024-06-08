package codezilla.handynestproject.dto.feedback;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FeedbackCreateRequestDto {

    @Schema(description = "Sender ID")
    @NotNull(message = "Sender ID can not be empty")
    private Long senderId;
    @Schema(description = "Feedback text")
    private String text;
    @Schema(description = "Feedback grade (0-5)", example = "5")
    @Min(0)
    @Max(5)
    private Long grade;
    @Schema(description = "Task ID")
    @NotNull(message = "Task ID can not be empty")
    private Long taskId;
}
