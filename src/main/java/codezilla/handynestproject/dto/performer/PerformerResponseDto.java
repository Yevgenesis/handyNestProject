package codezilla.handynestproject.dto.performer;

import codezilla.handynestproject.dto.category.CategoryTitleDto;
import codezilla.handynestproject.model.entity.Address;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;


@Data
public class PerformerResponseDto {

    private Long id;

    private String phoneNumber;

    private boolean isPhoneVerified;

    private boolean isPassportVerified;

    private String description;

    private Set<CategoryTitleDto> categories = new HashSet<>();

    @JsonFormat(shape = JsonFormat.Shape.STRING) // ToDo
    private Address address;

    boolean isAvailable;

    private Double positiveFeedbackPercent;

    private Long feedbackCount;

    private Timestamp createdOn;

    private Timestamp updatedOn;

}

