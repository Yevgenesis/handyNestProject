package codezilla.handynestproject.testData;

import codezilla.handynestproject.dto.user.UserNestedResponseDto;
import codezilla.handynestproject.dto.user.UserResponseDto;
import codezilla.handynestproject.model.entity.User;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class UserTestData {
    /**
     * UserResponseDTO
     */

    public static final UserResponseDto USER_RESPONSE_DTO1 = new UserResponseDto(
            1L, "Джон", "Доу", "john.doe@example.com", 5L, 100.0,
            null,
            Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(10, 0, 0))),
            Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(10, 0, 0))));

    public static final UserResponseDto USER_RESPONSE_DTO2 = new UserResponseDto(
            2L, "Джейн", "Смит", "jane.smith@example.com", 6L, 50.0,
            null,
            Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(11, 0, 0))),
            Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(11, 0, 0))));

    public static final UserResponseDto USER_RESPONSE_DTO3 = new UserResponseDto(
            3L, "Алиса", "Джонсон", "alice.johnson@example.com", 7L, 80.0,
            "logo/231434.jpeg",
            Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(12, 0, 0))),
            Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(12, 0, 0))));

    public static final UserResponseDto USER_RESPONSE_DTO4 = new UserResponseDto(
            4L, "Боб", "Уильямс", "bob.williams@example.com", 8L, 75.0,
            null,
            Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(13, 0, 0))),
            Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(13, 0, 0))));

    public static final UserResponseDto USER_RESPONSE_DTO5 = new UserResponseDto(
            5L, "Ева", "Браун", "eva.brown@example.com", 9L, 95.0,
            null,
            Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(14, 0, 0))),
            Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(14, 0, 0))));

    /**
     * UserNestedResponseDto
     */

    public static final UserNestedResponseDto USER_NESTED_RESPONSE_DTO1 = new UserNestedResponseDto(
            1L, "Джон", "Доу", 5L, 100.0,null);

    public static final UserNestedResponseDto USER_NESTED_RESPONSE_DTO2 = new UserNestedResponseDto(
            2L, "Джейн", "Смит", 6L, 50.0,null);

    public static final UserNestedResponseDto USER_NESTED_RESPONSE_DTO3 = new UserNestedResponseDto(
            3L, "Алиса", "Джонсон", 7L, 80.0, "logo/231434.jpeg");

    public static final UserNestedResponseDto USER_NESTED_RESPONSE_DTO4 = new UserNestedResponseDto(
            4L, "Боб", "Уильямс", 8L, 75.0,null);

    public static final UserNestedResponseDto USER_NESTED_RESPONSE_DTO5 = new UserNestedResponseDto(
            5L, "Ева", "Браун", 9L, 95.0, "logo/112233.jpeg");

    /**
     * test data User
     */

    public static final User TEST_USER1 = User.builder()
            .id(1L)
            .firstName(USER_RESPONSE_DTO1.getFirstName())
            .lastName(USER_RESPONSE_DTO1.getLastName())
            .email(USER_RESPONSE_DTO1.getEmail())
            .isEmailVerified(true)
            .password("password123")
            .created_on(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(10, 0, 0))))
            .updated_on(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(10, 0, 0))))
            .isDeleted(false)
            .build();

    public static final User TEST_USER2 = User.builder()
            .id(USER_RESPONSE_DTO2.getId())
            .firstName(USER_RESPONSE_DTO2.getFirstName())
            .lastName(USER_RESPONSE_DTO2.getLastName())
            .email(USER_RESPONSE_DTO2.getEmail())
            .isEmailVerified(true)
            .password("qwerty123")
            .created_on(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(11, 0, 0))))
            .updated_on(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(11, 0, 0))))
            .isDeleted(false)
            .build();
    public static final User TEST_USER3 = User.builder()
            .id(USER_RESPONSE_DTO3.getId())
            .firstName(USER_RESPONSE_DTO3.getFirstName())
            .lastName(USER_RESPONSE_DTO3.getLastName())
            .email(USER_RESPONSE_DTO3.getEmail())
            .isEmailVerified(true)
            .password("test123")
            .created_on(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(12, 0, 0))))
            .updated_on(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(12, 0, 0))))
            .isDeleted(false)
            .build();

    public static final User TEST_USER4 = User.builder()
            .id(USER_RESPONSE_DTO4.getId())
            .firstName(USER_RESPONSE_DTO4.getFirstName())
            .lastName(USER_RESPONSE_DTO4.getLastName())
            .email(USER_RESPONSE_DTO4.getEmail())
            .isEmailVerified(true)
            .password("password456")
            .created_on(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(13, 0, 0))))
            .updated_on(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(13, 0, 0))))
            .isDeleted(false)
            .build();

    public static final User TEST_USER5 = User.builder()
            .id(USER_RESPONSE_DTO5.getId())
            .firstName(USER_RESPONSE_DTO5.getFirstName())
            .lastName(USER_RESPONSE_DTO5.getLastName())
            .email(USER_RESPONSE_DTO5.getEmail())
            .isEmailVerified(true)
            .password("abc123")
            .created_on(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(14, 0, 0))))
            .updated_on(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(14, 0, 0))))
            .isDeleted(false)
            .build();
}
