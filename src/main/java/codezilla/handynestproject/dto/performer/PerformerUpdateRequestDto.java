package codezilla.handynestproject.dto.performer;

import codezilla.handynestproject.dto.address.AddressDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerformerUpdateRequestDto {

    @Schema(description = "Performer ID")
    @NotNull(message = "Performer ID can not be empty")
    private Long performerId;
    @Schema(description = "Phone number")
    @Size(min = 10, max = 25, message = "Phone number mast be minimum 10 and maximum 25 digits")
    private String phoneNumber;
    @Schema(description = "Performer description")
    @Size(min = 3, max = 150, message = "Description mast be minimum 3 and maximum 150 symbols")
    private String description;
    @Schema(description = "List of category IDs")
    private List<Long> categoryIDs = new ArrayList<>();
    @Schema(description = "Address details")
    @NotNull
    private AddressDto addressDto;
}

