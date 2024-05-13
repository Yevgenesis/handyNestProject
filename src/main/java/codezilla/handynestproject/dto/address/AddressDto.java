package codezilla.handynestproject.dto.address;

import lombok.Data;

@Data
public class AddressDto {
    private String street;
    private String city;
    private String zip;
    private String country;
}
