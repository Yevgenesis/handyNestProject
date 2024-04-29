package codezilla.handynestproject.dto;

import codezilla.handynestproject.model.entity.Feedback;
import codezilla.handynestproject.model.entity.enums.Rating;
import lombok.Data;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
public class UserResponseDto {

    private UUID id;

    private String firstName;

    private String lastName;

    private String email;

    private boolean isEmailVerified;

    private String password;

    private Timestamp created_on;

    private Timestamp updated_on;

    private boolean isDeleted;

    private String phoneNumber;

    private boolean isPhoneVerified;

    private boolean isPassportVerified;

    private Rating rating;

    private String country;

    private String city;

    private Set<Feedback> sentFeedbacks = new HashSet<>();

}
