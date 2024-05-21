package codezilla.handynestproject.dto.feedback;

import codezilla.handynestproject.dto.user.UserNestedResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeedbackResponseDto {

    private Long id;

    private UserNestedResponseDto sender;

    private String text;

    private Long grade;

    private Long taskId;

    private Timestamp createdOn;

}
