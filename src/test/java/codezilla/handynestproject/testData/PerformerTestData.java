package codezilla.handynestproject.testData;

import codezilla.handynestproject.dto.performer.PerformerNestedResponseDto;
import codezilla.handynestproject.dto.performer.PerformerRequestDto;
import codezilla.handynestproject.dto.performer.PerformerResponseDto;
import codezilla.handynestproject.dto.performer.PerformerUpdateRequestDto;
import codezilla.handynestproject.model.entity.Performer;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static codezilla.handynestproject.testData.AddressTestData.TEST_ADDRESS1;
import static codezilla.handynestproject.testData.AddressTestData.TEST_ADDRESS2;
import static codezilla.handynestproject.testData.AddressTestData.TEST_ADDRESS3;
import static codezilla.handynestproject.testData.AddressTestData.TEST_ADDRESS4;
import static codezilla.handynestproject.testData.AddressTestData.TEST_ADDRESS5;
import static codezilla.handynestproject.testData.AddressTestData.TEST_ADDRESS_DTO1;
import static codezilla.handynestproject.testData.AddressTestData.TEST_ADDRESS_DTO2;
import static codezilla.handynestproject.testData.AddressTestData.TEST_ADDRESS_DTO3;
import static codezilla.handynestproject.testData.AddressTestData.TEST_ADDRESS_DTO4;
import static codezilla.handynestproject.testData.AddressTestData.TEST_ADDRESS_DTO5;
import static codezilla.handynestproject.testData.CategoryTestData.CATEGORY_TITLE_DTO18;
import static codezilla.handynestproject.testData.CategoryTestData.CATEGORY_TITLE_DTO19;
import static codezilla.handynestproject.testData.CategoryTestData.CATEGORY_TITLE_DTO2;
import static codezilla.handynestproject.testData.CategoryTestData.CATEGORY_TITLE_DTO20;
import static codezilla.handynestproject.testData.CategoryTestData.CATEGORY_TITLE_DTO28;
import static codezilla.handynestproject.testData.CategoryTestData.CATEGORY_TITLE_DTO35;
import static codezilla.handynestproject.testData.CategoryTestData.CATEGORY_TITLE_DTO4;
import static codezilla.handynestproject.testData.CategoryTestData.CATEGORY_TITLE_DTO40;
import static codezilla.handynestproject.testData.CategoryTestData.CATEGORY_TITLE_DTO8;
import static codezilla.handynestproject.testData.CategoryTestData.TEST_CATEGORY18;
import static codezilla.handynestproject.testData.CategoryTestData.TEST_CATEGORY19;
import static codezilla.handynestproject.testData.CategoryTestData.TEST_CATEGORY2;
import static codezilla.handynestproject.testData.CategoryTestData.TEST_CATEGORY20;
import static codezilla.handynestproject.testData.CategoryTestData.TEST_CATEGORY28;
import static codezilla.handynestproject.testData.CategoryTestData.TEST_CATEGORY35;
import static codezilla.handynestproject.testData.CategoryTestData.TEST_CATEGORY4;
import static codezilla.handynestproject.testData.CategoryTestData.TEST_CATEGORY40;
import static codezilla.handynestproject.testData.CategoryTestData.TEST_CATEGORY8;
import static codezilla.handynestproject.testData.UserTestData.TEST_USER1;
import static codezilla.handynestproject.testData.UserTestData.TEST_USER2;
import static codezilla.handynestproject.testData.UserTestData.TEST_USER3;
import static codezilla.handynestproject.testData.UserTestData.TEST_USER4;
import static codezilla.handynestproject.testData.UserTestData.TEST_USER5;

public class PerformerTestData {
    /**
     * PerformerRequestDto
     */

    public static final PerformerRequestDto PERFORMER_REQUEST_DTO1 = new PerformerRequestDto(
            1L,
            "+49123456789",
            "Опытный сантехник с большим опытом работы",
            List.of(2L, 4L),
            TEST_ADDRESS_DTO1);

    public static final PerformerUpdateRequestDto PERFORMER_UPDATE_REQUEST_DTO1 = new PerformerUpdateRequestDto(
            1L,
            "+49123456789",
            "Опытный сантехник с большим опытом работы",
            List.of(2L, 4L),
            TEST_ADDRESS_DTO1);

    public static final PerformerRequestDto PERFORMER_REQUEST_DTO2 = new PerformerRequestDto(
            2L,
            "+49123456789",
            "Опытный маляр, предоставляю услуги качественной покраски стен",
            List.of(8L, 19L, 20L),
            TEST_ADDRESS_DTO2);

    public static final PerformerRequestDto PERFORMER_REQUEST_DTO3 = new PerformerRequestDto(
            3L,
            "+49123456789",
            "Электрик с опытом работы, устанавливаю различные светильники",
            List.of(8L),
            TEST_ADDRESS_DTO3);

    public static final PerformerRequestDto PERFORMER_REQUEST_DTO4 = new PerformerRequestDto(
            4L,
            "+49123456789",
            "Опытный сантехник, умею делать качественный ремонт сантехники",
            List.of(18L, 35L),
            TEST_ADDRESS_DTO4);

    public static final PerformerRequestDto PERFORMER_REQUEST_DTO5 = new PerformerRequestDto(
            5L,
            "+49123456789",
            "Опытный строитель, предоставляю услуги по строительству домов и квартир",
            List.of(28L, 40L),
            TEST_ADDRESS_DTO5);

    /**
     * PerformerResponseDto
     */

    public static final PerformerResponseDto PERFORMER_RESPONSE_DTO1 = new PerformerResponseDto(
            1L,
            "Джон",
            "Доу",
            "+49123456789",
            "Опытный сантехник с большим опытом работы",
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
            2L,
            "Джейн",
            "Смит",
            "+49123456789",
            "Опытный маляр, предоставляю услуги качественной покраски стен",
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
            3L,
            "Алиса",
            "Джонсон",
            "+49123456789",
            "Электрик с опытом работы, устанавливаю различные светильники",
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
            4L,
            "Боб",
            "Уильямс",
            "+49123456789",
            "Опытный сантехник, умею делать качественный ремонт сантехники",
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
            5L,
            "Ева",
            "Браун",
            "+49123456789",
            "Опытный строитель, предоставляю услуги по строительству домов и квартир",
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
     * PerformerNestedResponseDto
     */

    public static final PerformerNestedResponseDto PERFORMER_NESTED_RESPONSE_DTO1 = new PerformerNestedResponseDto(
            1L,
            "Джон",
            "Доу",
            5.0,
            100L);

    public static final PerformerNestedResponseDto PERFORMER_NESTED_RESPONSE_DTO2 = new PerformerNestedResponseDto(
            2L,
            "Джейн",
            "Смит",
            6.0,
            50L);

    public static final PerformerNestedResponseDto PERFORMER_NESTED_RESPONSE_DTO3 = new PerformerNestedResponseDto(
            3L,
            "Алиса",
            "Джонсон",
            7.0,
            80L);

    public static final PerformerNestedResponseDto PERFORMER_NESTED_RESPONSE_DTO4 = new PerformerNestedResponseDto(
            4L,
            "Боб",
            "Уильямс",
            8.0,
            75L);

    public static final PerformerNestedResponseDto PERFORMER_NESTED_RESPONSE_DTO5 = new PerformerNestedResponseDto(
            5L,
            "Ева",
            "Браун",
            9.0,
            95L);

    /**
     * test data Performer
     */

    public static final Performer TEST_PERFORMER1 = Performer.builder()
            .id(1L)
            .phoneNumber("+49123456789")
            .isPhoneVerified(true)
            .isPassportVerified(true)
            .description("Опытный сантехник с большим опытом работы")
            .isAvailable(true)
            .positiveFeedbackPercent(4.5)
            .taskCount(100L)
            .user(TEST_USER1)
            .categories(Set.of(TEST_CATEGORY2, TEST_CATEGORY4))
            .address(TEST_ADDRESS1)
            .createdOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(12, 0, 0))))
            .updatedOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(12, 0, 0))))
            .build();

    public static final Performer TEST_PERFORMER2 = Performer.builder()
            .id(2L)
            .phoneNumber("+49123456789")
            .isPhoneVerified(true)
            .isPassportVerified(true)
            .description("Опытный маляр, предоставляю услуги качественной покраски стен")
            .isAvailable(true)
            .positiveFeedbackPercent(4.8)
            .taskCount(150L)
            .user(TEST_USER2)
            .categories(Set.of(TEST_CATEGORY8, TEST_CATEGORY19, TEST_CATEGORY20))
            .address(TEST_ADDRESS2)
            .createdOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(13, 0, 0))))
            .updatedOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(13, 0, 0))))
            .build();

    public static final Performer TEST_PERFORMER3 = Performer.builder()
            .id(3L)
            .phoneNumber("+49123456789")
            .isPhoneVerified(true)
            .isPassportVerified(true)
            .description("Электрик с опытом работы, устанавливаю различные светильники")
            .isAvailable(true)
            .positiveFeedbackPercent(4.0)
            .taskCount(80L)
            .user(TEST_USER3)
            .categories(Set.of(TEST_CATEGORY8))
            .address(TEST_ADDRESS3)
            .createdOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(14, 0, 0))))
            .updatedOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(14, 0, 0))))
            .build();

    public static final Performer TEST_PERFORMER4 = Performer.builder()
            .id(4L)
            .phoneNumber("+49123456789")
            .isPhoneVerified(true)
            .isPassportVerified(true)
            .description("Опытный сантехник, умею делать качественный ремонт сантехники")
            .isAvailable(true)
            .positiveFeedbackPercent(4.7)
            .taskCount(120L)
            .user(TEST_USER4)
            .categories(Set.of(TEST_CATEGORY18, TEST_CATEGORY35, TEST_CATEGORY40))
            .address(TEST_ADDRESS4)
            .createdOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(15, 0, 0))))
            .updatedOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(15, 0, 0))))
            .build();

    public static final Performer TEST_PERFORMER5 = Performer.builder()
            .id(5L)
            .phoneNumber("+49123456789")
            .isPhoneVerified(true)
            .isPassportVerified(true)
            .description("Опытный строитель, предоставляю услуги по строительству домов и квартир")
            .isAvailable(true)
            .positiveFeedbackPercent(4.2)
            .taskCount(90L)
            .user(TEST_USER5)
            .categories(Set.of(TEST_CATEGORY28, TEST_CATEGORY40))
            .address(TEST_ADDRESS5)
            .createdOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(15, 0, 0))))
            .updatedOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 4, 29), LocalTime.of(15, 0, 0))))
            .build();

}
