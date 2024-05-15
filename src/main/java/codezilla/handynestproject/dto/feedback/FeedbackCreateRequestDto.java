package codezilla.handynestproject.dto.feedback;

import codezilla.handynestproject.model.enums.Grade;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeedbackCreateRequestDto {

    private Long senderId;

    private String text;

    private Grade grade;

    private Long taskId;

}
