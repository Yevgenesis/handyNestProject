package codezilla.handynestproject.dto.performer;

import codezilla.handynestproject.dto.address.AddressDto;
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

    @NotNull(message = "Performer ID can not be empty")
    private Long performerId;
    @Size(min = 10, max = 25, message = "Phone number mast be minimum 10 and maximum 25 digits")
    private String phoneNumber;

    @Size(min = 3, max = 150, message = "Description mast be minimum 3 and maximum 150 symbols")
    private String description;

    private List<Long> categoryIDs = new ArrayList<>();

    @NotNull
    private AddressDto addressDto;

}

