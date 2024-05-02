package codezilla.handynestproject.dto.user;

import codezilla.handynestproject.model.entity.enums.Rating;
import lombok.Data;

import java.sql.Timestamp;

// ToDo убрать конфиденциальные поля (оставил для тестов)
@Data
public class UserResponseDto {

    private Long id;

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

}

