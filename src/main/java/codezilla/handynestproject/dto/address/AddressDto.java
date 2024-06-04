package codezilla.handynestproject.dto.address;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AddressDto {
    @Schema(description = "Street name", example = "Main Street")
    private String street;
    @Schema(description = "City name", example = "New York")
    private String city;
    @Schema(description = "Zip code", example = "10001")
    private String zip;
    @Schema(description = "Country name", example = "USA")
    private String country;
}

