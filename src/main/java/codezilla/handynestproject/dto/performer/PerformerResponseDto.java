package codezilla.handynestproject.dto.performer;

import codezilla.handynestproject.dto.address.AddressDto;
import codezilla.handynestproject.dto.category.CategoryTitleDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class PerformerResponseDto {

    @Schema(description = "Performer ID")
    private Long id;
    @Schema(description = "Performer first name")
    private String firstName;
    @Schema(description = "Performer last name")
    private String lastName;
    @Schema(description = "Performer phone number")
    private String phoneNumber;
    @Schema(description = "Performer description")
    private String description;
    @Schema(description = "Performer categories")
    private Set<CategoryTitleDto> categories = new HashSet<>();
    @Schema(description = "Performer address")
    private AddressDto address;
    @Schema(description = "Performer availability")
    private boolean isAvailable;
    @Schema(description = "Performer positive feedback percentage")
    private Double positiveFeedbackPercent;
    @Schema(description = "Performer task count")
    private Long taskCount;
    @Schema(description = "Timestamp of performer creation")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy HH:mm:ss")
    private Timestamp createdOn;
    @Schema(description = "Timestamp of last performer update")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy HH:mm:ss")
    private Timestamp updatedOn;
}