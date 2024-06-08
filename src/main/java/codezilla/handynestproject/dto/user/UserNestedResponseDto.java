package codezilla.handynestproject.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserNestedResponseDto {

    @Schema(description = "User ID")
    private Long id;
    @Schema(description = "User first name")
    private String firstName;
    @Schema(description = "User last name")
    private String lastName;
    @Schema(description = "User task count")
    private Long taskCount;
    @Schema(description = "User positive feedback percentage")
    private Double positiveFeedbackPercent;
    @Schema(description = "User logo")
    private String logo;
}

