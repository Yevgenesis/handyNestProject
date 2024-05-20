package codezilla.handynestproject.service;

import codezilla.handynestproject.dto.address.AddressDto;
import codezilla.handynestproject.dto.category.CategoryResponseDto;
import codezilla.handynestproject.dto.category.CategoryTitleDto;
import codezilla.handynestproject.dto.feedback.FeedbackCreateRequestDto;
import codezilla.handynestproject.dto.feedback.FeedbackResponseDto;
import codezilla.handynestproject.dto.performer.PerformerRequestDto;
import codezilla.handynestproject.dto.performer.PerformerResponseDto;
import codezilla.handynestproject.dto.task.TaskRequestDto;
import codezilla.handynestproject.dto.task.TaskResponseDto;
import codezilla.handynestproject.dto.user.UserNestedResponseDto;
import codezilla.handynestproject.dto.user.UserResponseDto;
import codezilla.handynestproject.model.entity.*;
import codezilla.handynestproject.model.enums.Grade;
import codezilla.handynestproject.model.enums.TaskStatus;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

public class TestData {

    /**
     * test date WorkingTime
     */

    public static final WorkingTime TEST_WORKING_TIME1 = WorkingTime.builder()
            .id(1L).title("с 8 до 12").build();

    public static final WorkingTime TEST_WORKING_TIME2 = WorkingTime.builder()
            .id(2L).title("с 12 до 16").build();

    public static final WorkingTime TEST_WORKING_TIME3 = WorkingTime.builder()
            .id(3L).title("с 16 до 22").build();

    public static final WorkingTime TEST_WORKING_TIME4 = WorkingTime.builder()
            .id(4L).title("в любое время").build();


    /**
     * AddressDto
     */
    public static final AddressDto TEST_ADDRESS_DTO1 = new AddressDto(
            "123 Unter den Linden", "Berlin", "10117", "Germany");

    public static final AddressDto TEST_ADDRESS_DTO2 = new AddressDto(
            "456 Königsallee", "Düsseldorf", "40212", "Germany");

    public static final AddressDto TEST_ADDRESS_DTO3 = new AddressDto(
            "789 Karl-Liebknecht-Strasse", "Leipzig", "04109", "Germany");

    public static final AddressDto TEST_ADDRESS_DTO4 = new AddressDto(
            "321 Maximilianstrasse", "Munich", "80539", "Germany");

    public static final AddressDto TEST_ADDRESS_DTO5 = new AddressDto(
            "555 Friedrichstrasse", "Berlin", "10117", "Germany");

    /**
     * test data Address
     */

    public static final Address TEST_ADDRESS1 = new Address(
            1L, TEST_ADDRESS_DTO1.getStreet(), TEST_ADDRESS_DTO1.getCity(),
            TEST_ADDRESS_DTO1.getZip(), TEST_ADDRESS_DTO1.getCountry());

    public static final Address TEST_ADDRESS2 = new Address(
            2L, TEST_ADDRESS_DTO2.getStreet(), TEST_ADDRESS_DTO2.getCity(),
            TEST_ADDRESS_DTO2.getZip(), TEST_ADDRESS_DTO2.getCountry());

    public static final Address TEST_ADDRESS3 = new Address(
            3L, TEST_ADDRESS_DTO3.getStreet(), TEST_ADDRESS_DTO3.getCity(),
            TEST_ADDRESS_DTO3.getZip(), TEST_ADDRESS_DTO3.getCountry());

    public static final Address TEST_ADDRESS4 = new Address(
            4L, TEST_ADDRESS_DTO4.getStreet(), TEST_ADDRESS_DTO4.getCity(),
            TEST_ADDRESS_DTO4.getZip(), TEST_ADDRESS_DTO4.getCountry());

    public static final Address TEST_ADDRESS5 = new Address(
            5L, TEST_ADDRESS_DTO5.getStreet(), TEST_ADDRESS_DTO5.getCity(),
            TEST_ADDRESS_DTO5.getZip(), TEST_ADDRESS_DTO5.getCountry());

    /**
     * CategoryTitleDto
     */

    public static final CategoryTitleDto CATEGORY_TITLE_DTO2 = new CategoryTitleDto(
            2L,
            "Строительство");

    public static final CategoryTitleDto CATEGORY_TITLE_DTO4 = new CategoryTitleDto(
            4L,
            "Грузоперевозки");

    public static final CategoryTitleDto CATEGORY_TITLE_DTO8 = new CategoryTitleDto(
            8L,
            "Дизайн интерьера");

    public static final CategoryTitleDto CATEGORY_TITLE_DTO18 = new CategoryTitleDto(
            18L,
            "Капитальный ремонт");

    public static final CategoryTitleDto CATEGORY_TITLE_DTO19 = new CategoryTitleDto(
            19L,
            "Ремонт кухни");

    public static final CategoryTitleDto CATEGORY_TITLE_DTO20 = new CategoryTitleDto(
            20L,
            "Ремонт ванной");

    public static final CategoryTitleDto CATEGORY_TITLE_DTO28 = new CategoryTitleDto(
            28L,
            "Офисный переезд");

    public static final CategoryTitleDto CATEGORY_TITLE_DTO35 = new CategoryTitleDto(
            35L,
            "Уход за больными");

    public static final CategoryTitleDto CATEGORY_TITLE_DTO40 = new CategoryTitleDto(
            40L,
            "Дизайн офисов");

    /**
     * test data Category
     */

    public static final Category TEST_CATEGORY2 = Category.builder()
            .id(CATEGORY_TITLE_DTO2.getId())
            .title(CATEGORY_TITLE_DTO2.getTitle())
            .parentId(null)
            .weight(20)
            .build();

    public static final Category TEST_CATEGORY4 = Category.builder()
            .id(CATEGORY_TITLE_DTO4.getId())
            .title(CATEGORY_TITLE_DTO4.getTitle())
            .parentId(null)
            .weight(40)
            .build();

    public static final Category TEST_CATEGORY8 = Category.builder()
            .id(CATEGORY_TITLE_DTO8.getId())
            .title(CATEGORY_TITLE_DTO8.getTitle())
            .parentId(null)
            .weight(80)
            .build();

    public static final Category TEST_CATEGORY18 = Category.builder()
            .id(CATEGORY_TITLE_DTO18.getId())
            .title(CATEGORY_TITLE_DTO18.getTitle())
            .parentId(1L)
            .weight(70)
            .build();

    public static final Category TEST_CATEGORY19 = Category.builder()
            .id(CATEGORY_TITLE_DTO19.getId())
            .title(CATEGORY_TITLE_DTO19.getTitle())
            .parentId(1L)
            .weight(80)
            .build();

    public static final Category TEST_CATEGORY20 = Category.builder()
            .id(CATEGORY_TITLE_DTO20.getId())
            .title(CATEGORY_TITLE_DTO20.getTitle())
            .parentId(1L)
            .weight(90)
            .build();

    public static final Category TEST_CATEGORY28 = Category.builder()
            .id(CATEGORY_TITLE_DTO28.getId())
            .title(CATEGORY_TITLE_DTO28.getTitle())
            .parentId(4L)
            .weight(20)
            .build();

    public static final Category TEST_CATEGORY35 = Category.builder()
            .id(CATEGORY_TITLE_DTO35.getId())
            .title(CATEGORY_TITLE_DTO35.getTitle())
            .parentId(6L)
            .weight(30)
            .build();

    public static final Category TEST_CATEGORY40 = Category.builder()
            .id(CATEGORY_TITLE_DTO40.getId())
            .title(CATEGORY_TITLE_DTO40.getTitle())
            .parentId(8L)
            .weight(30)
            .build();

    /**
     * CategoryResponseDTO
     */

    public static final CategoryResponseDto CATEGORY_RESPONSE_DTO2 = new CategoryResponseDto(
            2L,
            "Строительство",
            null,
            20);

    public static final CategoryResponseDto CATEGORY_RESPONSE_DTO4 = new CategoryResponseDto(
            4L,
            "Грузоперевозки",
            null,
            40);

    public static final CategoryResponseDto CATEGORY_RESPONSE_DTO8 = new CategoryResponseDto(
            8L,
            "Дизайн интерьера",
            null,
            80);

    public static final CategoryResponseDto CATEGORY_RESPONSE_DTO18 = new CategoryResponseDto(
            18L,
            "Капитальный ремонт",
            null,
            70);

    public static final CategoryResponseDto CATEGORY_RESPONSE_DTO19 = new CategoryResponseDto(
            19L,
            "Ремонт кухни",
            null,
            80);

    public static final CategoryResponseDto CATEGORY_RESPONSE_DTO20 = new CategoryResponseDto(
            20L,
            "Ремонт ванной",
            null,
            90);

    public static final CategoryResponseDto CATEGORY_RESPONSE_DTO28 = new CategoryResponseDto(
            28L,
            "Офисный переезд",
            null,
            20);

    public static final CategoryResponseDto CATEGORY_RESPONSE_DTO35 = new CategoryResponseDto(
            35L,
            "Уход за больными",
            null,
            30);

    /**
     * UserResponseDTO
     */

    public static final UserResponseDto USER_RESPONSE_DTO1 = new UserResponseDto(
            1L, "Джон", "Доу", "john.doe@example.com",
            Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(10, 0, 0))),
            Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(10, 0, 0))));

    public static final UserResponseDto USER_RESPONSE_DTO2 = new UserResponseDto(
            2L, "Джейн", "Смит", "jane.smith@example.com",
            Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(11, 0, 0))),
            Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(11, 0, 0))));

    public static final UserResponseDto USER_RESPONSE_DTO3 = new UserResponseDto(
            3L, "Алиса", "Джонсон", "alice.johnson@example.com",
            Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(12, 0, 0))),
            Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(12, 0, 0))));

    public static final UserResponseDto USER_RESPONSE_DTO4 = new UserResponseDto(
            4L, "Боб", "Уильямс", "bob.williams@example.com",
            Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(13, 0, 0))),
            Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(13, 0, 0))));

    public static final UserResponseDto USER_RESPONSE_DTO5 = new UserResponseDto(
            5L, "Ева", "Браун", "eva.brown@example.com",
            Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(14, 0, 0))),
            Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(14, 0, 0))));

    /**
     * UserNestedResponseDto
     */

    public static final UserNestedResponseDto USER_NESTED_RESPONSE_DTO1 = new UserNestedResponseDto(
            1L, "Джон","Доу");

    public static final UserNestedResponseDto USER_NESTED_RESPONSE_DTO2 = new UserNestedResponseDto(
            2L, "Джейн","Смит");

    public static final UserNestedResponseDto USER_NESTED_RESPONSE_DTO3 = new UserNestedResponseDto(
            3L, "Алиса","Джонсон");

    public static final UserNestedResponseDto USER_NESTED_RESPONSE_DTO4 = new UserNestedResponseDto(
            4L, "Боб","Уильямс");

    public static final UserNestedResponseDto USER_NESTED_RESPONSE_DTO5 = new UserNestedResponseDto(
            5L, "Ева","Браун");

    /**
     * test data User
     */

    public static final User TEST_USER1 = User.builder()
            .id(USER_RESPONSE_DTO1.getId())
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

    /**
     * PerformerRequestDto
     */

    public static final PerformerRequestDto PERFORMER_REQUEST_DTO1 = new PerformerRequestDto(
            1L,
            "+49123456789",
            "Опытный сантехник с большим опытом работы",
            List.of(TEST_CATEGORY2.getId(), TEST_CATEGORY4.getId()),
            TEST_ADDRESS_DTO1);

    public static final PerformerRequestDto PERFORMER_REQUEST_DTO2 = new PerformerRequestDto(
            2L,
            "+49123456789",
            "Опытный маляр, предоставляю услуги качественной покраски стен",
            List.of(TEST_CATEGORY8.getId(), TEST_CATEGORY19.getId(), TEST_CATEGORY20.getId()),
            TEST_ADDRESS_DTO2);

    public static final PerformerRequestDto PERFORMER_REQUEST_DTO3 = new PerformerRequestDto(
            3L,
            "+49123456789",
            "Электрик с опытом работы, устанавливаю различные светильники",
            List.of(TEST_CATEGORY8.getId()),
            TEST_ADDRESS_DTO3);

    public static final PerformerRequestDto PERFORMER_REQUEST_DTO4 = new PerformerRequestDto(
            4L,
            "+49123456789",
            "Опытный сантехник, умею делать качественный ремонт сантехники",
            List.of(TEST_CATEGORY18.getId(), TEST_CATEGORY35.getId()),
            TEST_ADDRESS_DTO4);

    public static final PerformerRequestDto PERFORMER_REQUEST_DTO5 = new PerformerRequestDto(
            5L,
            "+49123456789",
            "Опытный строитель, предоставляю услуги по строительству домов и квартир",
            List.of(TEST_CATEGORY40.getId(), TEST_CATEGORY28.getId()),
            TEST_ADDRESS_DTO5
    );

    /**
     * PerformerResponseDto
     */

    public static final PerformerResponseDto PERFORMER_RESPONSE_DTO1 = new PerformerResponseDto(
            TEST_USER1.getId(),
            TEST_USER1.getFirstName(),
            TEST_USER1.getLastName(),
            PERFORMER_REQUEST_DTO1.getPhoneNumber(),
            PERFORMER_REQUEST_DTO1.getDescription(),
            Set.of(CATEGORY_TITLE_DTO2, CATEGORY_TITLE_DTO4),
            TEST_ADDRESS_DTO1,
            true,
            4.5,
            100L,
            Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(12, 0, 0))),
            Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(12, 0, 0)))

    );

    public static final PerformerResponseDto PERFORMER_RESPONSE_DTO2 = new PerformerResponseDto(
            TEST_USER2.getId(),
            TEST_USER2.getFirstName(),
            TEST_USER2.getLastName(),
            PERFORMER_REQUEST_DTO2.getPhoneNumber(),
            PERFORMER_REQUEST_DTO2.getDescription(),
            Set.of(CATEGORY_TITLE_DTO19, CATEGORY_TITLE_DTO8, CATEGORY_TITLE_DTO20),
            TEST_ADDRESS_DTO2,
            true,
            4.8,
            150L,
            Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(13, 0, 0))),
            Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(13, 0, 0))));

    public static final PerformerResponseDto PERFORMER_RESPONSE_DTO3 = new PerformerResponseDto(
            TEST_USER3.getId(),
            TEST_USER3.getFirstName(),
            TEST_USER3.getLastName(),
            PERFORMER_REQUEST_DTO3.getPhoneNumber(),
            PERFORMER_REQUEST_DTO3.getDescription(),
            Set.of(CATEGORY_TITLE_DTO8),
            TEST_ADDRESS_DTO3,
            true,
            4.0,
            80L,
            Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(14, 0, 0))),
            Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(14, 0, 0))));

    public static final PerformerResponseDto PERFORMER_RESPONSE_DTO4 = new PerformerResponseDto(
            TEST_USER4.getId(),
            TEST_USER4.getFirstName(),
            TEST_USER4.getLastName(),
            PERFORMER_REQUEST_DTO4.getPhoneNumber(),
            PERFORMER_REQUEST_DTO4.getDescription(),
            Set.of(CATEGORY_TITLE_DTO18, CATEGORY_TITLE_DTO35),
            TEST_ADDRESS_DTO4,
            true,
            4.7,
            120L,
            Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(15, 0, 0))),
            Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(15, 0, 0))));

    public static final PerformerResponseDto PERFORMER_RESPONSE_DTO5 = new PerformerResponseDto(
            TEST_USER5.getId(),
            TEST_USER5.getFirstName(),
            TEST_USER5.getLastName(),
            PERFORMER_REQUEST_DTO5.getPhoneNumber(),
            PERFORMER_REQUEST_DTO5.getDescription(),
            Set.of(CATEGORY_TITLE_DTO28, CATEGORY_TITLE_DTO40),
            TEST_ADDRESS_DTO5,
            true,
            4.2,
            90L,
            Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(16, 0, 0))),
            Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(16, 0, 0))));

    /**
     * test data Performer
     */

    public static final Performer TEST_PERFORMER1 = Performer.builder()
                      .phoneNumber(PERFORMER_RESPONSE_DTO1.getPhoneNumber())
            .isPhoneVerified(true)
            .isPassportVerified(true)
            .description(PERFORMER_RESPONSE_DTO1.getDescription())
            .isAvailable(true)
            .positiveFeedbackPercent(PERFORMER_RESPONSE_DTO1.getPositiveFeedbackPercent())
            .feedbackCount(PERFORMER_RESPONSE_DTO1.getFeedbackCount())
            .user(TEST_USER1)
            .categories(Set.of(TEST_CATEGORY2, TEST_CATEGORY4))
            .address(TEST_ADDRESS1)
            .createdOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(12, 0, 0))))
            .updatedOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(12, 0, 0))))
            .build();

    public static final Performer TEST_PERFORMER2 = Performer.builder()
            .id(PERFORMER_RESPONSE_DTO2.getId())
            .phoneNumber(PERFORMER_RESPONSE_DTO2.getPhoneNumber())
            .isPhoneVerified(true)
            .isPassportVerified(true)
            .description(PERFORMER_RESPONSE_DTO2.getDescription())
            .isAvailable(true)
            .positiveFeedbackPercent(PERFORMER_RESPONSE_DTO2.getPositiveFeedbackPercent())
            .feedbackCount(PERFORMER_RESPONSE_DTO2.getFeedbackCount())
            .user(TEST_USER2)
            .categories(Set.of(TEST_CATEGORY8, TEST_CATEGORY19, TEST_CATEGORY20))
            .address(TEST_ADDRESS2)
            .createdOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(13, 0, 0))))
            .updatedOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(13, 0, 0))))
            .build();

    public static final Performer TEST_PERFORMER3 = Performer.builder()
            .id(PERFORMER_RESPONSE_DTO3.getId())
            .phoneNumber(PERFORMER_RESPONSE_DTO3.getPhoneNumber())
            .isPhoneVerified(true)
            .isPassportVerified(true)
            .description(PERFORMER_RESPONSE_DTO3.getDescription())
            .isAvailable(true)
            .positiveFeedbackPercent(PERFORMER_RESPONSE_DTO3.getPositiveFeedbackPercent())
            .feedbackCount(PERFORMER_RESPONSE_DTO1.getFeedbackCount())
            .user(TEST_USER3)
            .categories(Set.of(TEST_CATEGORY8))
            .address(TEST_ADDRESS3)
            .createdOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(14, 0, 0))))
            .updatedOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(14, 0, 0))))
            .build();

    public static final Performer TEST_PERFORMER4 = Performer.builder()
            .id(PERFORMER_RESPONSE_DTO4.getId())
            .phoneNumber(PERFORMER_RESPONSE_DTO4.getPhoneNumber())
            .isPhoneVerified(true)
            .isPassportVerified(true)
            .description(PERFORMER_RESPONSE_DTO4.getDescription())
            .isAvailable(true)
            .positiveFeedbackPercent(PERFORMER_RESPONSE_DTO4.getPositiveFeedbackPercent())
            .feedbackCount(PERFORMER_RESPONSE_DTO4.getFeedbackCount())
            .user(TEST_USER4)
            .categories(Set.of(TEST_CATEGORY18, TEST_CATEGORY35, TEST_CATEGORY40))
            .address(TEST_ADDRESS4)
            .createdOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(15, 0, 0))))
            .updatedOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(15, 0, 0))))
            .build();

    public static final Performer TEST_PERFORMER5 = Performer.builder()
            .id(PERFORMER_RESPONSE_DTO5.getId())
            .phoneNumber(PERFORMER_RESPONSE_DTO5.getPhoneNumber())
            .isPhoneVerified(true)
            .isPassportVerified(true)
            .description(PERFORMER_RESPONSE_DTO5.getDescription())
            .isAvailable(true)
            .positiveFeedbackPercent(PERFORMER_RESPONSE_DTO5.getPositiveFeedbackPercent())
            .feedbackCount(PERFORMER_RESPONSE_DTO5.getFeedbackCount())
            .user(TEST_USER5)
            .categories(Set.of(TEST_CATEGORY28, TEST_CATEGORY40))
            .address(TEST_ADDRESS5)
            .createdOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(15, 0, 0))))
            .updatedOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(15, 0, 0))))
            .build();


    /**
     * TaskRequestDto
     */

    public static final TaskRequestDto TASK_REQUEST_DTO1 = new TaskRequestDto(
            "Починить кран",
            "Требуется починить кран на кухне",
            50.0,
            TEST_ADDRESS1.getCountry(),
            TEST_ADDRESS1.getCity(),
            TEST_ADDRESS1.getStreet(),
            TEST_ADDRESS1.getZip(),
            TEST_WORKING_TIME1.getId(),
            TEST_USER1.getId(),
            TEST_CATEGORY19.getId(),
            true);

    public static final TaskRequestDto TASK_REQUEST_DTO2 = new TaskRequestDto(
            "Покрасить комнату",
            "Нужно покрасить стены в гостиной",
            200.0,
            TEST_ADDRESS2.getCountry(),
            TEST_ADDRESS2.getCity(),
            TEST_ADDRESS2.getStreet(),
            TEST_ADDRESS2.getZip(),
            TEST_WORKING_TIME2.getId(),
            TEST_USER2.getId(),
            TEST_CATEGORY8.getId(),
            true);

    public static final TaskRequestDto TASK_REQUEST_DTO3 = new TaskRequestDto(
            "Установить светильники",
            "Требуется установить новые светильники в коридоре",
            100.0,
            TEST_ADDRESS3.getCountry(),
            TEST_ADDRESS3.getCity(),
            TEST_ADDRESS3.getStreet(),
            TEST_ADDRESS3.getZip(),
            TEST_WORKING_TIME3.getId(),
            TEST_USER3.getId(),
            TEST_CATEGORY2.getId(),
            true);

    public static final TaskRequestDto TASK_REQUEST_DTO4 = new TaskRequestDto(
            "Офисный переезд",
            "Требуется упаковать мебель и вещи",
            500.0,
            TEST_ADDRESS4.getCountry(),
            TEST_ADDRESS4.getCity(),
            TEST_ADDRESS4.getStreet(),
            TEST_ADDRESS4.getZip(),
            TEST_WORKING_TIME4.getId(),
            TEST_USER4.getId(),
            TEST_CATEGORY28.getId(),
            true);

    public static final TaskRequestDto TASK_REQUEST_DTO5 = new TaskRequestDto(
            "Установить светильники",
            "Хочу установить старые светильники в сарае",
            100.0,
            TEST_ADDRESS5.getCountry(),
            TEST_ADDRESS5.getCity(),
            TEST_ADDRESS5.getStreet(),
            TEST_ADDRESS5.getZip(),
            TEST_WORKING_TIME4.getId(),
            TEST_USER5.getId(),
            TEST_CATEGORY18.getId(),
            true
    );

    /**
     * TaskResponseDto
     */


    public static final TaskResponseDto TASK_RESPONSE_DTO1 = TaskResponseDto.builder()
            .id(1L)
            .title(TASK_REQUEST_DTO1.title())
            .description(TASK_REQUEST_DTO1.description())
            .price(TASK_REQUEST_DTO1.price())
            .address(TEST_ADDRESS_DTO1)
            .taskStatus(TaskStatus.OPEN)
            .workingTime(TEST_WORKING_TIME1)
            .category(CATEGORY_TITLE_DTO19)
            .user(USER_NESTED_RESPONSE_DTO1)
            .performer(null)
            .isPublish(true)
            .createdOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 5, 14), LocalTime.of(12, 0, 0))))
            .updatedOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 5, 14), LocalTime.of(12, 0, 0))))
            .build();

    public static final TaskResponseDto TASK_RESPONSE_DTO2 = TaskResponseDto.builder()
            .id(2L)
            .title(TASK_REQUEST_DTO2.title())
            .description(TASK_REQUEST_DTO2.description())
            .price(TASK_REQUEST_DTO2.price())
            .address(TEST_ADDRESS_DTO2)
            .taskStatus(TaskStatus.IN_PROGRESS)
            .workingTime(TEST_WORKING_TIME2)
            .category(CATEGORY_TITLE_DTO8)
            .user(USER_NESTED_RESPONSE_DTO2)
            .performer(PERFORMER_RESPONSE_DTO4)
            .isPublish(true)
            .createdOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 5, 14), LocalTime.of(12, 0, 0))))
            .updatedOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 5, 14), LocalTime.of(12, 0, 0))))
            .build();

    public static final TaskResponseDto TASK_RESPONSE_DTO3 = TaskResponseDto.builder()
            .id(3L)
            .title(TASK_REQUEST_DTO3.title())
            .description(TASK_REQUEST_DTO3.description())
            .price(TASK_REQUEST_DTO3.price())
            .address(TEST_ADDRESS_DTO3)
            .taskStatus(TaskStatus.COMPLETED)
            .workingTime(TEST_WORKING_TIME3)
            .category(CATEGORY_TITLE_DTO2)
            .user(USER_NESTED_RESPONSE_DTO3)
            .performer(PERFORMER_RESPONSE_DTO5)
            .isPublish(true)
            .createdOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 5, 14), LocalTime.of(12, 0, 0))))
            .updatedOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 5, 14), LocalTime.of(12, 0, 0))))
            .build();

    public static final TaskResponseDto TASK_RESPONSE_DTO4 = TaskResponseDto.builder()
            .id(4L)
            .title(TASK_REQUEST_DTO4.title())
            .description(TASK_REQUEST_DTO4.description())
            .price(TASK_REQUEST_DTO4.price())
            .address(TEST_ADDRESS_DTO4)
            .taskStatus(TaskStatus.OPEN)
            .workingTime(TEST_WORKING_TIME4)
            .category(CATEGORY_TITLE_DTO28)
            .user(USER_NESTED_RESPONSE_DTO4)
            .performer(PERFORMER_RESPONSE_DTO2)
            .isPublish(true)
            .createdOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 5, 14), LocalTime.of(12, 0, 0))))
            .updatedOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 5, 14), LocalTime.of(12, 0, 0))))
            .build();

    public static final TaskResponseDto TASK_RESPONSE_DTO5 = TaskResponseDto.builder()
            .id(5L)
            .title(TASK_REQUEST_DTO5.title())
            .description(TASK_REQUEST_DTO5.description())
            .price(TASK_REQUEST_DTO5.price())
            .address(TEST_ADDRESS_DTO5)
            .taskStatus(TaskStatus.CANCELED)
            .workingTime(TEST_WORKING_TIME4)
            .category(CATEGORY_TITLE_DTO18)
            .user(USER_NESTED_RESPONSE_DTO5)
            .performer(PERFORMER_RESPONSE_DTO1)
            .isPublish(true)
            .createdOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 5, 14), LocalTime.of(12, 0, 0))))
            .updatedOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 5, 14), LocalTime.of(12, 0, 0))))
            .build();


    /**
     * test data Task
     */

    public static final Task TEST_TASK1 = Task.builder()
            .id(1L)
            .title(TASK_REQUEST_DTO1.title())
            .description(TASK_REQUEST_DTO1.description())
            .price(TASK_REQUEST_DTO1.price())
            .address(Address.builder()
                    .id(1L)
                    .country(TASK_REQUEST_DTO1.country())
                    .city(TASK_REQUEST_DTO1.city())
                    .street(TASK_REQUEST_DTO1.street())
                    .zip(TASK_REQUEST_DTO1.zip())
                    .build())
            .taskStatus(TaskStatus.OPEN)
            .isPublish(TASK_REQUEST_DTO1.isPublish())
            .workingTime(TEST_WORKING_TIME1)
            .category(TEST_CATEGORY19)
            .user(TEST_USER1)
            .performer(null)
            .createdOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 5, 14), LocalTime.of(12, 0, 0))))
            .updatedOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 5, 14), LocalTime.of(12, 0, 0))))
            .build();

    public static final Task TEST_TASK2 = Task.builder()
            .id(2L)
            .title(TASK_REQUEST_DTO2.title())
            .description(TASK_REQUEST_DTO2.description())
            .price(TASK_REQUEST_DTO2.price())
            .address(Address.builder()
                    .id(2L)
                    .country(TASK_REQUEST_DTO2.country())
                    .city(TASK_REQUEST_DTO2.city())
                    .street(TASK_REQUEST_DTO2.street())
                    .zip(TASK_REQUEST_DTO2.zip())
                    .build())
            .taskStatus(TaskStatus.IN_PROGRESS)
            .isPublish(TASK_REQUEST_DTO2.isPublish())
            .workingTime(TEST_WORKING_TIME2)
            .category(TEST_CATEGORY8)
            .user(TEST_USER2)
            .performer(TEST_PERFORMER4)
            .createdOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 5, 14), LocalTime.of(12, 0, 0))))
            .updatedOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 5, 14), LocalTime.of(12, 0, 0))))
            .build();

    public static final Task TEST_TASK3 = Task.builder()
            .id(3L)
            .title(TASK_REQUEST_DTO3.title())
            .description(TASK_REQUEST_DTO3.description())
            .price(TASK_REQUEST_DTO3.price())
            .address(Address.builder()
                    .id(3L)
                    .country(TASK_REQUEST_DTO3.country())
                    .city(TASK_REQUEST_DTO3.city())
                    .street(TASK_REQUEST_DTO3.street())
                    .zip(TASK_REQUEST_DTO3.zip())
                    .build())
            .taskStatus(TaskStatus.COMPLETED)
            .isPublish(TASK_REQUEST_DTO3.isPublish())
            .workingTime(TEST_WORKING_TIME3)
            .category(TEST_CATEGORY2)
            .user(TEST_USER3)
            .performer(TEST_PERFORMER5)
            .createdOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 5, 14), LocalTime.of(12, 0, 0))))
            .updatedOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 5, 14), LocalTime.of(12, 0, 0))))
            .build();

    public static final Task TEST_TASK4 = Task.builder()
            .id(4L)
            .title(TASK_REQUEST_DTO4.title())
            .description(TASK_REQUEST_DTO4.description())
            .price(TASK_REQUEST_DTO4.price())
            .address(Address.builder()
                    .id(4L)
                    .country(TASK_REQUEST_DTO4.country())
                    .city(TASK_REQUEST_DTO4.city())
                    .street(TASK_REQUEST_DTO4.street())
                    .zip(TASK_REQUEST_DTO4.zip())
                    .build())
            .taskStatus(TaskStatus.OPEN)
            .isPublish(TASK_REQUEST_DTO4.isPublish())
            .workingTime(TEST_WORKING_TIME4)
            .category(TEST_CATEGORY28)
            .user(TEST_USER4)
            .performer(TEST_PERFORMER2)
            .createdOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 5, 14), LocalTime.of(12, 0, 0))))
            .updatedOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 5, 14), LocalTime.of(12, 0, 0))))
            .build();

    public static final Task TEST_TASK5 = Task.builder()
            .id(5L)
            .title(TASK_REQUEST_DTO5.title())
            .description(TASK_REQUEST_DTO5.description())
            .price(TASK_REQUEST_DTO5.price())
            .address(Address.builder()
                    .id(5L)
                    .country(TASK_REQUEST_DTO5.country())
                    .city(TASK_REQUEST_DTO5.city())
                    .street(TASK_REQUEST_DTO5.street())
                    .zip(TASK_REQUEST_DTO5.zip())
                    .build())
            .taskStatus(TaskStatus.CANCELED)
            .isPublish(TASK_REQUEST_DTO5.isPublish())
            .workingTime(TEST_WORKING_TIME4)
            .category(TEST_CATEGORY18)
            .user(TEST_USER5)
            .performer(TEST_PERFORMER1)
            .createdOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 5, 14), LocalTime.of(12, 0, 0))))
            .updatedOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 5, 14), LocalTime.of(12, 0, 0))))
            .build();

    /**
     * Feedback
     */

    public static final Feedback TEST_FEEDBACK1 = Feedback.builder()
            .id(1L)
            .text("Отличная работа!")
            .sender(TEST_USER1)
            .grade(Grade.STAR5)
            .task(TEST_TASK4)
            .createdOn( Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 5, 14), LocalTime.of(12, 0, 0))))

            .build(); public static final Feedback TEST_FEEDBACK2 = Feedback.builder()
            .id(2L)
            .sender(TEST_USER3)
            .text("Очень впечатляющая работа!")
            .grade(Grade.STAR4)
            .task(TEST_TASK3)
            .createdOn( Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 5, 14), LocalTime.of(12, 15, 0))))
            .build();

            public static final Feedback TEST_FEEDBACK3 = Feedback.builder()
            .id(3L)
            .sender(TEST_USER1)
            .text(null)
            .grade(Grade.STAR3)
            .task(TEST_TASK2)
            .createdOn( Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 5, 14), LocalTime.of(12, 30, 0))))
            .build();

            public static final Feedback TEST_FEEDBACK4 = Feedback.builder()
            .id(4L)
            .sender(TEST_USER3)
            .text("Отличный исполнитель!")
            .grade(Grade.STAR5)
            .task(TEST_TASK3)
            .createdOn( Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 5, 14), LocalTime.of(12, 30, 0))))

            .build();

            public static final Feedback TEST_FEEDBACK5 = Feedback.builder()
            .id(5L)
            .sender(TEST_USER2)
            .text("Требуется улучшение")
            .grade(Grade.STAR1)
            .task(TEST_TASK1)
            .createdOn( Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 5, 14), LocalTime.of(13, 0, 0))))
            .build();

    /**
     * FeedbackCreateRequestDto
     */

    public static final FeedbackCreateRequestDto FEEDBACK_REQUEST_DTO1 = new FeedbackCreateRequestDto(
            TEST_USER1.getId(),"Отличная работа!",Grade.STAR5,TEST_TASK4.getId());

    public static final FeedbackCreateRequestDto FEEDBACK_REQUEST_DTO2 = new FeedbackCreateRequestDto(
            TEST_USER3.getId(),"Очень впечатляющая работа!",Grade.STAR4,TEST_TASK3.getId());

    public static final FeedbackCreateRequestDto FEEDBACK_REQUEST_DTO3 = new FeedbackCreateRequestDto(
            TEST_USER1.getId(),null,Grade.STAR3,TEST_TASK2.getId());

    public static final FeedbackCreateRequestDto FEEDBACK_REQUEST_DTO4 = new FeedbackCreateRequestDto(
            TEST_USER3.getId(),"Отличный заказчик!",Grade.STAR5,TEST_TASK5.getId());

    public static final FeedbackCreateRequestDto FEEDBACK_REQUEST_DTO5 = new FeedbackCreateRequestDto(
            TEST_USER2.getId(),"Требуется улучшение",Grade.STAR1,TEST_TASK1.getId());

/**
 * FeedbackResponseDto
 */

    public static final FeedbackResponseDto FEEDBACK_RESPONSE_DTO1 = new FeedbackResponseDto(
        1L,USER_NESTED_RESPONSE_DTO1,FEEDBACK_REQUEST_DTO1.getText(),FEEDBACK_REQUEST_DTO1.getGrade(),
        TEST_TASK4.getId(), Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                (2024, 5, 14), LocalTime.of(12, 0, 0))));

    public static final FeedbackResponseDto FEEDBACK_RESPONSE_DTO2 = new FeedbackResponseDto(
        2L,USER_NESTED_RESPONSE_DTO3,FEEDBACK_REQUEST_DTO2.getText(),FEEDBACK_REQUEST_DTO2.getGrade(),
            TEST_TASK3.getId(), Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                (2024, 5, 14), LocalTime.of(12, 15, 0))));

  public static final FeedbackResponseDto FEEDBACK_RESPONSE_DTO3 = new FeedbackResponseDto(
        3L,USER_NESTED_RESPONSE_DTO1,FEEDBACK_REQUEST_DTO3.getText(),FEEDBACK_REQUEST_DTO3.getGrade(),
          TEST_TASK2.getId(), Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                (2024, 5, 14), LocalTime.of(12, 30, 0))));

  public static final FeedbackResponseDto FEEDBACK_RESPONSE_DTO4 = new FeedbackResponseDto(
        4L,USER_NESTED_RESPONSE_DTO3,FEEDBACK_REQUEST_DTO4.getText(),FEEDBACK_REQUEST_DTO4.getGrade(),
          TEST_TASK4.getId(), Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                (2024, 5, 14), LocalTime.of(12, 30, 0))));

  public static final FeedbackResponseDto FEEDBACK_RESPONSE_DTO5 = new FeedbackResponseDto(
        5L,USER_NESTED_RESPONSE_DTO2,FEEDBACK_REQUEST_DTO5.getText(),FEEDBACK_REQUEST_DTO5.getGrade(),
          TEST_TASK1.getId(), Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                (2024, 5, 14), LocalTime.of(13, 0, 0))));



}
