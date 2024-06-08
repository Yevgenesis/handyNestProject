package codezilla.handynestproject.dto.performer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class PerformerNestedResponseDto {

    @Schema(description = "Performer ID")
    private Long id;
    @Schema(description = "Performer first name")
    private String firstName;
    @Schema(description = "Performer last name")
    private String lastName;
    @Schema(description = "Performer positive feedback percentage")
    private Double positiveFeedbackPercent;
    @Schema(description = "Performer task count")
    private Long taskCount;

}

