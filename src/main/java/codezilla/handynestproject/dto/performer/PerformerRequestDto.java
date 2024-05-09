package codezilla.handynestproject.dto.performer;

import codezilla.handynestproject.dto.address.AddressDto;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;


@Data
public class PerformerRequestDto {

    private Long userId;

    private String phoneNumber;

    private String description;

    private Set<Long> categoryIDs = new HashSet<>();

    // Address
//    private String street;
//
//    private String city;
//
//    private String country;
//
//    private String zip;

    private AddressDto addressDto;

}

