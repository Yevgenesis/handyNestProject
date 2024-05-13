package codezilla.handynestproject.service;

import codezilla.handynestproject.dto.category.CategoryTitleDto;
import codezilla.handynestproject.dto.performer.PerformerResponseDto;
import codezilla.handynestproject.dto.task.TaskRequestDto;
import codezilla.handynestproject.dto.task.TaskResponseDto;
import codezilla.handynestproject.dto.user.UserResponseDto;
import codezilla.handynestproject.model.entity.Address;
import codezilla.handynestproject.model.entity.WorkingTime;
import codezilla.handynestproject.model.enums.TaskStatus;

public class TestData {
    public static final TaskRequestDto TASK_REQUEST_DTO = new TaskRequestDto(
            "Test Title",
            "Test Description",
            20.0,
            "Test Country",
            "Test City",
            "Test Street",
            "Test zip",
            1L,
            1L,
            1L,
            true
    );

    public static final TaskResponseDto TASK_RESPONSE_DTO = new TaskResponseDto(
            1L,
            "Test Title",
            "Test Description",
            20.0,
            new Address(1L,
                    "Test Street",
                    "Test City",
                    "Test zip",
                    "Test Country"
            ),
            TaskStatus.OPEN,
            new WorkingTime(),
            new CategoryTitleDto(),
            new UserResponseDto(),
            new PerformerResponseDto(),
            true


    );


}
