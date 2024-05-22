package codezilla.handynestproject.dto.performer;

import codezilla.handynestproject.dto.address.AddressDto;
import codezilla.handynestproject.dto.category.CategoryTitleDto;
import com.fasterxml.jackson.annotation.JsonFormat;
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

    private Long id;

    private String firstName;

    private String lastName;

    private String phoneNumber;

//    private boolean isPhoneVerified;
//
//    private boolean isPassportVerified;

    private String description;

    private Set<CategoryTitleDto> categories = new HashSet<>();

    //        @JsonFormat(shape = JsonFormat.Shape.STRING) // ToDo
    private AddressDto address;

    boolean isAvailable;

    private Double positiveFeedbackPercent;

    private Long taskCount;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy HH:mm:ss")
    private Timestamp createdOn;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy HH:mm:ss")
    private Timestamp updatedOn;

}

