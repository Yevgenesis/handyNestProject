package codezilla.handynestproject.service;

import codezilla.handynestproject.dto.address.AddressDto;
import codezilla.handynestproject.dto.category.CategoryTitleDto;
import codezilla.handynestproject.dto.performer.PerformerResponseDto;
import codezilla.handynestproject.dto.task.TaskRequestDto;
import codezilla.handynestproject.dto.task.TaskResponseDto;
import codezilla.handynestproject.dto.user.UserResponseDto;
import codezilla.handynestproject.model.entity.Address;
import codezilla.handynestproject.model.entity.Category;
import codezilla.handynestproject.model.entity.Performer;
import codezilla.handynestproject.model.entity.Task;
import codezilla.handynestproject.model.entity.User;
import codezilla.handynestproject.model.entity.WorkingTime;
import codezilla.handynestproject.model.enums.TaskStatus;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

public class TestData {

    public static final WorkingTime TEST_WORKING_TIME = WorkingTime.builder()
            .id(1L)
            .title("с 8 до 12")
            .build();

    public static final Category TEST_CATEGORY = Category.builder()
            .id(1L)
            .title("Test Title")
            .parentId(null)
            .weight(10)
            .build();
    public static final AddressDto ADDRESS_DTO = new AddressDto(
            "Test Street",
            "Test City",
            "Test zip",
            "Test Country"
    );
    public static final Address TEST_ADDRESS = new Address(
            1L,
            "Test Street",
            "Test City",
            "Test zip",
            "Test Country"
    );
    public static final User TEST_USER = User.builder()
            .id(1L)
            .firstName("Test First Name")
            .lastName("Test Last Name")
            .email("Test Email")
            .password("Test Password")
            .isDeleted(false)
            .build();

    public static final Performer TEST_PERFORMER = Performer.builder()
            .phoneNumber("Test Phone Number")
            .description("Test Description")
            .user(TEST_USER)
            .categories(Set.of(TEST_CATEGORY))
            .address(TEST_ADDRESS)
            .build();

    public static final CategoryTitleDto CATEGORY_TITLE_DTO = new CategoryTitleDto(
            1L,
            "Test Title"
    );

    public static final UserResponseDto USER_RESPONSE_DTO = new UserResponseDto(
            1L,
            "Test First Name",
            "Test Last Name",
            "Test Email",
            Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2001, 1, 1), LocalTime.of(8, 45, 0))),
            Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2001, 1, 1), LocalTime.of(8, 45, 0)))

    );


    public static final PerformerResponseDto PERFORMER_RESPONSE_DTO = new PerformerResponseDto(
            1L,
            "Test First Name",
            "Test Last Name",
            "Test Phone Number",
            "Test Description",
            Set.of(CATEGORY_TITLE_DTO),
            ADDRESS_DTO,
            true,
            0.0,
            0L,
            Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2001, 1, 1), LocalTime.of(8, 45, 0))),
            Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2001, 1, 1), LocalTime.of(8, 45, 0)))

    );

    public static final TaskRequestDto TASK_REQUEST_DTO = new TaskRequestDto(
            "Test Title",
            "Test Description",
            0.0,
            "Test Country",
            "Test City",
            "Test Street",
            "Test zip",
            1L,
            1L,
            1L,
            true
    );
//TODO записать все дто, аналогично адресДто
    public static final TaskResponseDto TASK_RESPONSE_DTO = new TaskResponseDto(
            1L,
            "Test Title",
            "Test Description",
            0.0,
            ADDRESS_DTO,
            TaskStatus.OPEN,
            TEST_WORKING_TIME,
            CATEGORY_TITLE_DTO,
            USER_RESPONSE_DTO,
            PERFORMER_RESPONSE_DTO,
            true
    );


    public static final Task TEST_TASK = Task.builder()
            .title(TASK_REQUEST_DTO.title())
            .description(TASK_REQUEST_DTO.description())
            .price(TASK_REQUEST_DTO.price())
            .address(Address.builder()
                    .id(1L)
                    .country(TASK_REQUEST_DTO.country())
            .city(TASK_REQUEST_DTO.city())
            .street(TASK_REQUEST_DTO.street())
            .zip(TASK_REQUEST_DTO.zip())
                    .build())
            .taskStatus(TaskStatus.OPEN)
            .isPublish(TASK_REQUEST_DTO.isPublish())
            .workingTime(TEST_WORKING_TIME)
            .category(TEST_CATEGORY)
            .user(TEST_USER)
            .build();
}
