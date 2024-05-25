package codezilla.handynestproject.dto.feedback;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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

    @NotNull(message = "Sender ID can not be empty")
    private Long senderId;

    private String text;

    @Min(0)
    @Max(5)
    private Long grade;

    @NotNull(message = "Task ID can not be empty")
    private Long taskId;

}
