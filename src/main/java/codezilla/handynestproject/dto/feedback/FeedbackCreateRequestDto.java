package codezilla.handynestproject.dto.feedback;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FeedbackCreateRequestDto {

    private Long senderId;

    private String text;

    @Min(0)
    @Max(5)
    private Long grade;

    private Long taskId;

}
