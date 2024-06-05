package codezilla.handynestproject.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

// ToDo убрать конфиденциальные поля (оставил для тестов)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDto {

    @Schema(description = "User ID")
    private Long id;
    @Schema(description = "User first name")
    private String firstName;
    @Schema(description = "User last name")
    private String lastName;
    @Schema(description = "User email")
    private String email;
    @Schema(description = "User task count")
    private Long taskCount;
    @Schema(description = "User positive feedback percentage")
    private Double positiveFeedbackPercent;
    @Schema(description = "User logo")
    private String logo;
    @Schema(description = "Timestamp of user creation")
    private Timestamp created_on;
    @Schema(description = "Timestamp of last user update")
    private Timestamp updated_on;
}

