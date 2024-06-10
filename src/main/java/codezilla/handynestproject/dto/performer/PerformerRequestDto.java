package codezilla.handynestproject.dto.performer;

import codezilla.handynestproject.dto.address.AddressDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
public class PerformerRequestDto {

    @Schema(description = "User ID")
    @NotNull(message = "User ID can not be empty")
    private Long userId;

    @Schema(description = "Phone number")
    @NotBlank(message = "Invalid Phone number: Empty number")
    @Size(min = 10, max = 25, message = "Phone number mast be minimum 10 and maximum 25 digits")
    private String phoneNumber;

    @Schema(description = "Performer description")
    @Size(min = 3, max = 150, message = "Description mast be minimum 3 and maximum 150 symbols")
    private String description;

    @NotEmpty(message = "Categories can`t be empty")
    @Schema(description = "List of category IDs")
    private List<Long> categoryIDs = new ArrayList<>();

    @NotNull
    @Schema(description = "Address details")
    private AddressDto addressDto;
}

