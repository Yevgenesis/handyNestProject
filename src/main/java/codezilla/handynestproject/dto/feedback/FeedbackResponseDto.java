package codezilla.handynestproject.dto.feedback;

import codezilla.handynestproject.model.enums.Grade;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeedbackResponseDto {

    private Long id;

    private Long senderId;

    private String text;

    private Grade grade;

    private Long taskId;

    private Timestamp createdOn;

}
