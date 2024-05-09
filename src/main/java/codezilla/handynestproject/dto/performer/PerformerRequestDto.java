package codezilla.handynestproject.dto.performer;

import codezilla.handynestproject.dto.address.AddressDto;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;


@Data
public class PerformerRequestDto {

    private Long userId;

    private String phoneNumber;

    private String description;

    private List<Long> categoryIDs = new ArrayList<>();

    private AddressDto addressDto;

}

