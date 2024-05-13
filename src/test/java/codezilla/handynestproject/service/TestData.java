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
            .created_on(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2001, 1, 1), LocalTime.of(8, 45, 0))))
            .updated_on(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2001, 1, 1), LocalTime.of(8, 45, 0))))
            .isDeleted(false)
            .build();

    public static final User TEST_USER2 = User.builder()
            .id(2L)
            .firstName("Test First Name2")
            .lastName("Test Last Name2")
            .email("Test Email2")
            .password("Test Password2")
            .created_on(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2001, 1, 1), LocalTime.of(8, 45, 0))))
            .updated_on(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2001, 1, 1), LocalTime.of(8, 45, 0))))
            .isDeleted(false)
            .build();

    public static final Performer TEST_PERFORMER = Performer.builder()
            .id(1L)
            .phoneNumber("Test Phone Number")
            .description("Test Description")
            .user(TEST_USER)
            .categories(Set.of(TEST_CATEGORY))
            .address(TEST_ADDRESS)
            .isAvailable(true)
            .createdOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2001, 1, 1), LocalTime.of(8, 45, 0))))
            .updatedOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2001, 1, 1), LocalTime.of(8, 45, 0))))
            .build();
    public static final Performer TEST_PERFORMER2 = Performer.builder()
            .id(2L)
            .phoneNumber("Test Phone Number2")
            .description("Test Description2")
            .user(TEST_USER2)
            .categories(Set.of(TEST_CATEGORY))
            .address(TEST_ADDRESS)
            .isAvailable(true)
            .createdOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2001, 1, 1), LocalTime.of(8, 45, 0))))
            .updatedOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2001, 1, 1), LocalTime.of(8, 45, 0))))
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
    public static final UserResponseDto USER_RESPONSE_DTO2 = new UserResponseDto(
            2L,
            "Test First Name2",
            "Test Last Name2",
            "Test Email2",
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

    public static final PerformerResponseDto PERFORMER_RESPONSE_DTO2 = new PerformerResponseDto(
            2L,
            "Test First Name2",
            "Test Last Name2",
            "Test Phone Number2",
            "Test Description2",
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

    public static final TaskResponseDto TASK_RESPONSE_DTO = TaskResponseDto.builder()
            .id(1L)
            .title("Test Title")
            .description("Test Description")
            .price(0.0)
            .address(ADDRESS_DTO)
            .taskStatus(TaskStatus.OPEN)
            .workingTime(TEST_WORKING_TIME)
            .category(CATEGORY_TITLE_DTO)
            .user(USER_RESPONSE_DTO)
            .performer(null)
            .isPublish(true)
            .build();

    public static final TaskResponseDto TASK_RESPONSE_DTO_WITH_PERFORMER = TaskResponseDto.builder()
            .id(2L)
            .title("Test Title")
            .description("Test Description")
            .price(0.0)
            .address(ADDRESS_DTO)
            .taskStatus(TaskStatus.IN_PROGRESS)
            .workingTime(TEST_WORKING_TIME)
            .category(CATEGORY_TITLE_DTO)
            .user(USER_RESPONSE_DTO)
            .performer(PERFORMER_RESPONSE_DTO2)
            .isPublish(true)
            .build();



    public static final Task TEST_TASK_OPEN = Task.builder()
            .id(1L)
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
            .performer(null)
            .build();

    public static final Task TEST_TASK2_IN_PROGRESS = Task.builder()
            .id(2L)
            .title(TASK_REQUEST_DTO.title())
            .description(TASK_REQUEST_DTO.description())
            .price(TASK_REQUEST_DTO.price())
            .address(Address.builder()
                    .id(2L)
                    .country(TASK_REQUEST_DTO.country())
            .city(TASK_REQUEST_DTO.city())
            .street(TASK_REQUEST_DTO.street())
            .zip(TASK_REQUEST_DTO.zip())
                    .build())
            .taskStatus(TaskStatus.IN_PROGRESS)
            .isPublish(TASK_REQUEST_DTO.isPublish())
            .workingTime(TEST_WORKING_TIME)
            .category(TEST_CATEGORY)
            .user(TEST_USER)
            .performer(TEST_PERFORMER2)
            .build();


}
