package codezilla.handynestproject.dto.user;

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



}

