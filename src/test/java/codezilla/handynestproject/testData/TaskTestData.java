package codezilla.handynestproject.testData;

import codezilla.handynestproject.dto.task.TaskRequestDto;
import codezilla.handynestproject.dto.task.TaskResponseDto;
import codezilla.handynestproject.model.entity.Address;
import codezilla.handynestproject.model.entity.Task;
import codezilla.handynestproject.model.enums.TaskStatus;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static codezilla.handynestproject.testData.AddressTestData.TEST_ADDRESS_DTO1;
import static codezilla.handynestproject.testData.AddressTestData.TEST_ADDRESS_DTO2;
import static codezilla.handynestproject.testData.AddressTestData.TEST_ADDRESS_DTO3;
import static codezilla.handynestproject.testData.AddressTestData.TEST_ADDRESS_DTO4;
import static codezilla.handynestproject.testData.AddressTestData.TEST_ADDRESS_DTO5;
import static codezilla.handynestproject.testData.CategoryTestData.CATEGORY_TITLE_DTO18;
import static codezilla.handynestproject.testData.CategoryTestData.CATEGORY_TITLE_DTO19;
import static codezilla.handynestproject.testData.CategoryTestData.CATEGORY_TITLE_DTO2;
import static codezilla.handynestproject.testData.CategoryTestData.CATEGORY_TITLE_DTO28;
import static codezilla.handynestproject.testData.CategoryTestData.CATEGORY_TITLE_DTO8;
import static codezilla.handynestproject.testData.CategoryTestData.TEST_CATEGORY18;
import static codezilla.handynestproject.testData.CategoryTestData.TEST_CATEGORY19;
import static codezilla.handynestproject.testData.CategoryTestData.TEST_CATEGORY2;
import static codezilla.handynestproject.testData.CategoryTestData.TEST_CATEGORY28;
import static codezilla.handynestproject.testData.CategoryTestData.TEST_CATEGORY8;
import static codezilla.handynestproject.testData.PerformerTestData.PERFORMER_NESTED_RESPONSE_DTO1;
import static codezilla.handynestproject.testData.PerformerTestData.PERFORMER_NESTED_RESPONSE_DTO2;
import static codezilla.handynestproject.testData.PerformerTestData.PERFORMER_NESTED_RESPONSE_DTO3;
import static codezilla.handynestproject.testData.PerformerTestData.PERFORMER_NESTED_RESPONSE_DTO4;
import static codezilla.handynestproject.testData.PerformerTestData.PERFORMER_NESTED_RESPONSE_DTO5;
import static codezilla.handynestproject.testData.PerformerTestData.TEST_PERFORMER2;
import static codezilla.handynestproject.testData.PerformerTestData.TEST_PERFORMER4;
import static codezilla.handynestproject.testData.PerformerTestData.TEST_PERFORMER5;
import static codezilla.handynestproject.testData.UserTestData.TEST_USER1;
import static codezilla.handynestproject.testData.UserTestData.TEST_USER2;
import static codezilla.handynestproject.testData.UserTestData.TEST_USER3;
import static codezilla.handynestproject.testData.UserTestData.TEST_USER4;
import static codezilla.handynestproject.testData.UserTestData.TEST_USER5;
import static codezilla.handynestproject.testData.UserTestData.USER_NESTED_RESPONSE_DTO1;
import static codezilla.handynestproject.testData.UserTestData.USER_NESTED_RESPONSE_DTO2;
import static codezilla.handynestproject.testData.UserTestData.USER_NESTED_RESPONSE_DTO3;
import static codezilla.handynestproject.testData.UserTestData.USER_NESTED_RESPONSE_DTO5;
import static codezilla.handynestproject.testData.WorkingTimeTestData.TEST_WORKING_TIME1;
import static codezilla.handynestproject.testData.WorkingTimeTestData.TEST_WORKING_TIME2;
import static codezilla.handynestproject.testData.WorkingTimeTestData.TEST_WORKING_TIME3;
import static codezilla.handynestproject.testData.WorkingTimeTestData.TEST_WORKING_TIME4;

public class TaskTestData {
    /**
     * TaskRequestDto
     */

    public static final TaskRequestDto TASK_REQUEST_DTO1 = new TaskRequestDto(
            "Починить кран",
            "Требуется починить кран на кухне",
            50.0,
            "123 Unter den Linden", "Berlin", "10117", "Germany",
            1L,
            5L,
            19L,
            true);

    public static final TaskRequestDto TASK_REQUEST_DTO2 = new TaskRequestDto(
            "Покрасить комнату",
            "Нужно покрасить стены в гостиной",
            200.0,
            "456 Königsallee", "Düsseldorf", "40212", "Germany",
            2L,
            3L,
            8L,
            true);

    public static final TaskRequestDto TASK_REQUEST_DTO3 = new TaskRequestDto(
            "Установить светильники",
            "Требуется установить новые светильники в коридоре",
            100.0,
            "789 Karl-Liebknecht-Strasse", "Leipzig", "04109", "Germany",
            3L,
            3L,
            2L,
            true);

    public static final TaskRequestDto TASK_REQUEST_DTO4 = new TaskRequestDto(
            "Офисный переезд",
            "Требуется упаковать мебель и вещи",
            500.0,
            "321 Maximilianstrasse", "Munich", "80539", "Germany",
            4L,
            2L,
            28L,
            true);

    public static final TaskRequestDto TASK_REQUEST_DTO5 = new TaskRequestDto(
            "Установить светильники",
            "Хочу установить старые светильники в сарае",
            100.0,
            "555 Friedrichstrasse", "Berlin", "10117", "Germany",
            4L,
            1L,
            18L,
            true);

    /**
     * TaskResponseDto
     */


    public static final TaskResponseDto TASK_RESPONSE_DTO1 = TaskResponseDto.builder()
            .id(1L)
            .title("Починить кран")
            .description("Требуется починить кран на кухне")
            .price(50.00)
            .address(TEST_ADDRESS_DTO1)
            .taskStatus(TaskStatus.OPEN)
            .workingTime(TEST_WORKING_TIME1)
            .category(CATEGORY_TITLE_DTO19)
            .user(USER_NESTED_RESPONSE_DTO5)
            .performer(null)
            .isPublish(true)
            .createdOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 5, 14), LocalTime.of(12, 0, 0))))
            .updatedOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 5, 14), LocalTime.of(12, 0, 0))))
            .build();

    public static final TaskResponseDto TASK_RESPONSE_DTO2 = TaskResponseDto.builder()
            .id(2L)
            .title("Покрасить комнату")
            .description("Нужно покрасить стены в гостиной")
            .price(200.00)
            .address(TEST_ADDRESS_DTO2)
            .taskStatus(TaskStatus.IN_PROGRESS)
            .workingTime(TEST_WORKING_TIME2)
            .category(CATEGORY_TITLE_DTO8)
            .user(USER_NESTED_RESPONSE_DTO3)
            .performer(PERFORMER_NESTED_RESPONSE_DTO4)
            .isPublish(true)
            .createdOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 5, 14), LocalTime.of(12, 0, 0))))
            .updatedOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 5, 14), LocalTime.of(12, 0, 0))))
            .build();

    public static final TaskResponseDto TASK_RESPONSE_DTO3 = TaskResponseDto.builder()
            .id(3L)
            .title("Установить светильники")
            .description("Требуется установить новые светильники в коридоре")
            .price(100.00)
            .address(TEST_ADDRESS_DTO3)
            .taskStatus(TaskStatus.COMPLETED)
            .workingTime(TEST_WORKING_TIME3)
            .category(CATEGORY_TITLE_DTO2)
            .user(USER_NESTED_RESPONSE_DTO3)
            .performer(PERFORMER_NESTED_RESPONSE_DTO5)
            .isPublish(true)
            .createdOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 5, 14), LocalTime.of(12, 0, 0))))
            .updatedOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 5, 14), LocalTime.of(12, 0, 0))))
            .build();

    public static final TaskResponseDto TASK_RESPONSE_DTO4 = TaskResponseDto.builder()
            .id(4L)
            .title("Офисный переезд")
            .description("Требуется упаковать мебель и вещи")
            .price(500.00)
            .address(TEST_ADDRESS_DTO4)
            .taskStatus(TaskStatus.OPEN)
            .workingTime(TEST_WORKING_TIME4)
            .category(CATEGORY_TITLE_DTO28)
            .user(USER_NESTED_RESPONSE_DTO2)
            .performer(null)
            .isPublish(true)
            .createdOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 5, 14), LocalTime.of(12, 0, 0))))
            .updatedOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 5, 14), LocalTime.of(12, 0, 0))))
            .build();

    public static final TaskResponseDto TASK_RESPONSE_DTO5 = TaskResponseDto.builder()
            .id(5L)
            .title("Установить светильники")
            .description("Хочу установить старые светильники в сарае")
            .price(100.00)
            .address(TEST_ADDRESS_DTO5)
            .taskStatus(TaskStatus.CANCELED)
            .workingTime(TEST_WORKING_TIME4)
            .category(CATEGORY_TITLE_DTO18)
            .user(USER_NESTED_RESPONSE_DTO1)
            .performer(PERFORMER_NESTED_RESPONSE_DTO2)
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
            .title("Починить кран")
            .description("Требуется починить кран на кухне")
            .price(50.00)
            .address(Address.builder()
                    .id(1L)
                    .country("Germany")
                    .city("Berlin")
                    .street("123 Unter den Linden")
                    .zip("10117")
                    .build())
            .taskStatus(TaskStatus.OPEN)
            .isPublish(true)
            .workingTime(TEST_WORKING_TIME1)
            .category(TEST_CATEGORY19)
            .user(TEST_USER5)
            .performer(null)
            .createdOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 5, 14), LocalTime.of(12, 0, 0))))
            .updatedOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 5, 14), LocalTime.of(12, 0, 0))))
            .build();

    public static final Task TEST_TASK2 = Task.builder()
            .id(2L)
            .title("Покрасить комнату")
            .description("Нужно покрасить стены в гостиной")
            .price(200.00)
            .address(Address.builder()
                    .id(2L)
                    .country("Germany")
                    .city("Düsseldorf")
                    .street("456 Königsallee")
                    .zip("40212")
                    .build())
            .taskStatus(TaskStatus.IN_PROGRESS)
            .isPublish(true)
            .workingTime(TEST_WORKING_TIME2)
            .category(TEST_CATEGORY8)
            .user(TEST_USER3)
            .performer(TEST_PERFORMER4)
            .createdOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 5, 14), LocalTime.of(12, 0, 0))))
            .updatedOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 5, 14), LocalTime.of(12, 0, 0))))
            .build();

    public static final Task TEST_TASK3 = Task.builder()
            .id(3L)
            .title("Установить светильники")
            .description("Требуется установить новые светильники в коридоре")
            .price(100.00)
            .address(Address.builder()
                    .id(3L)
                    .country("Germany")
                    .city("Leipzig")
                    .street("789 Karl-Liebknecht-Strasse")
                    .zip("04109")
                    .build())
            .taskStatus(TaskStatus.COMPLETED)
            .isPublish(true)
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
            .title("Офисный переезд")
            .description("Требуется упаковать мебель и вещи")
            .price(500.00)
            .address(Address.builder()
                    .id(4L)
                    .country("Germany")
                    .city("Munich")
                    .street("321 Maximilianstrasse")
                    .zip("80539")
                    .build())
            .taskStatus(TaskStatus.OPEN)
            .isPublish(true)
            .workingTime(TEST_WORKING_TIME4)
            .category(TEST_CATEGORY28)
            .user(TEST_USER2)
            .performer(null)
            .createdOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 5, 14), LocalTime.of(12, 0, 0))))
            .updatedOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 5, 14), LocalTime.of(12, 0, 0))))
            .build();

    public static final Task TEST_TASK5 = Task.builder()
            .id(5L)
            .title("Установить светильники")
            .description("Хочу установить старые светильники в сарае")
            .price(100.00)
            .address(Address.builder()
                    .id(5L)
                    .country("Germany")
                    .city("Berlin")
                    .street("555 Friedrichstrasse")
                    .zip("10117")
                    .build())
            .taskStatus(TaskStatus.CANCELED)
            .isPublish(true)
            .workingTime(TEST_WORKING_TIME4)
            .category(TEST_CATEGORY18)
            .user(TEST_USER1)
            .performer(TEST_PERFORMER2)
            .createdOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 5, 14), LocalTime.of(12, 0, 0))))
            .updatedOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 5, 14), LocalTime.of(12, 0, 0))))
            .build();

}
