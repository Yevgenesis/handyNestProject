package codezilla.handynestproject.dto.performer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class PerformerNestedResponseDto {

    private Long id;

    private String firstName;

    private String lastName;

//    private Set<CategoryTitleDto> categories = new HashSet<>();

    private Double positiveFeedbackPercent;

    private Long taskCount;

}

