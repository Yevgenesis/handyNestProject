package codezilla.handynestproject.controller;

import codezilla.handynestproject.HandyNestProjectApplication;
import codezilla.handynestproject.dto.task.TaskRequestDto;
import codezilla.handynestproject.dto.task.TaskResponseDto;
import codezilla.handynestproject.dto.task.TaskUpdateRequestDto;
import codezilla.handynestproject.exception.PerformerNotFoundException;
import codezilla.handynestproject.exception.TaskNotFoundException;
import codezilla.handynestproject.exception.UserNotFoundException;
import codezilla.handynestproject.model.enums.TaskStatus;
import codezilla.handynestproject.repository.TaskRepository;
import codezilla.handynestproject.service.TaskService;
import codezilla.handynestproject.util.TestDatabaseConfig;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static codezilla.handynestproject.service.TestData.TASK_REQUEST_DTO1;
import static codezilla.handynestproject.service.TestData.TASK_RESPONSE_DTO1;
import static codezilla.handynestproject.service.TestData.TASK_RESPONSE_DTO2;
import static codezilla.handynestproject.service.TestData.TASK_RESPONSE_DTO3;
import static codezilla.handynestproject.service.TestData.TASK_RESPONSE_DTO4;
import static codezilla.handynestproject.service.TestData.TEST_ADDRESS_DTO1;
import static codezilla.handynestproject.service.TestData.TEST_PERFORMER2;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
@ContextConfiguration(classes = {TestDatabaseConfig.class, HandyNestProjectApplication.class})

class TaskControllerTest {

    private final TaskService taskService;
    private final TaskRepository taskRepository;

    @Autowired
    TaskControllerTest(TaskService taskService, TaskRepository taskRepository) {
        this.taskService = taskService;
        this.taskRepository = taskRepository;
    }

    @Test
    @Transactional
    void getAllTest() {

        List<TaskResponseDto> actual = taskService.getAll();
        assertEquals(5, actual.size());

    }

    @Test
    @SneakyThrows
    void getByIdTest() {

        TaskResponseDto expectedTask = TASK_RESPONSE_DTO3;
        Long taskId = expectedTask.getId();

        TaskResponseDto actualTask = taskService.getById(taskId);
        assertEquals(expectedTask, actualTask);

        Long notExistingTaskId = 999L;
        assertThrows(TaskNotFoundException.class, () -> taskService.getById(notExistingTaskId));
    }

    @Test
    @SneakyThrows
    void getAvailableTest() {

        List<TaskResponseDto> expectedTasks = List.of(TASK_RESPONSE_DTO1, TASK_RESPONSE_DTO4);
        List<TaskResponseDto> actualTasks = taskService.getAvailableTasks();
        assertEquals(expectedTasks, actualTasks);
    }

    @Test
    @SneakyThrows
    void getByUserIdTest() {

        List<TaskResponseDto> expectedTasks = List.of(TASK_RESPONSE_DTO2);
        Long userId = TASK_RESPONSE_DTO2.getUser().getId();
        List<TaskResponseDto> actualTasks = taskService.getByUserId(userId);
        assertEquals(expectedTasks, actualTasks);

        Long notExistingTaskId = 999L;
        assertThrows(UserNotFoundException.class, () -> taskService.getByUserId(notExistingTaskId));

    }

    @Test
    @SneakyThrows
    void getByPerformerId() {
        List<TaskResponseDto> expectedTasks = List.of(TASK_RESPONSE_DTO2);
        Long performerId = TASK_RESPONSE_DTO2.getPerformer().getId();
        List<TaskResponseDto> actualTasks = taskService.getByPerformerId(performerId);
        assertEquals(expectedTasks, actualTasks);

        Long notExistingTaskId = 999L;
        assertThrows(PerformerNotFoundException.class, () -> taskService
                .getByPerformerId(notExistingTaskId));

    }

    @Test
    @SneakyThrows
    void getByStatus() {

        List<TaskResponseDto> expectedTasks = List.of(TASK_RESPONSE_DTO2);
        TaskStatus taskStatus = TaskStatus.IN_PROGRESS;
        List<TaskResponseDto> actualTasks = taskService.getByStatus(taskStatus);
        assertEquals(expectedTasks.size(), actualTasks.size());//если не сравнивать размеры листов, то не проходит

//        TaskStatus invalidStatus = TaskStatus.CANCELED;
//        assertThrows(TaskWrongStatusException.class, () -> taskService.getByStatus(invalidStatus));
    }

    @Test
    @SneakyThrows
    void create() {

        TaskRequestDto task = TASK_REQUEST_DTO1;
        TaskResponseDto expectedTask = TASK_RESPONSE_DTO1;
        Long taskId = expectedTask.getId();
        Long newId = taskId + 5L;
        expectedTask.setId(newId);
        TaskResponseDto actualTask = taskService.create(task);

        Timestamp createdOn1 = Timestamp.valueOf(LocalDateTime.now()
                .truncatedTo(ChronoUnit.MINUTES));
        expectedTask.setCreatedOn(createdOn1);
        Timestamp updatedOn1 = Timestamp.valueOf(LocalDateTime.now()
                .truncatedTo(ChronoUnit.MINUTES));
        expectedTask.setUpdatedOn(updatedOn1);

        Timestamp createdOn = Timestamp.valueOf(LocalDateTime.now()
                .truncatedTo(ChronoUnit.MINUTES));
        actualTask.setCreatedOn(createdOn);
        Timestamp updatedOn = Timestamp.valueOf(LocalDateTime.now()
                .truncatedTo(ChronoUnit.MINUTES));
        actualTask.setUpdatedOn(updatedOn);

        assertEquals(expectedTask, actualTask);
    }




    @Test
    @SneakyThrows
    void updateTest() {
        TaskUpdateRequestDto task = new TaskUpdateRequestDto(
                1L,
                "Test Title",
                "Test Description",
                0.0,
                TEST_ADDRESS_DTO1,
                2L);

        TaskResponseDto expectedTask = TASK_RESPONSE_DTO1;
        TaskResponseDto actualTask = taskService.update(task);
        assertEquals(expectedTask, actualTask);


    }

    @Test
    @SneakyThrows
    void addPerformerTest() {
        TaskResponseDto task = TASK_RESPONSE_DTO1;
        Long taskId = task.getId();
        Long performerId = TEST_PERFORMER2.getId();
        TaskResponseDto expectedTask = TASK_RESPONSE_DTO4;
        TaskResponseDto actualTask = taskService.addPerformer(taskId, performerId);
        assertEquals(expectedTask.getPerformer(), actualTask.getPerformer());


    }

    @Test
    @SneakyThrows
    void removePerformer() {
        TaskResponseDto task = TASK_RESPONSE_DTO1;
        Long taskId = task.getId();
        TaskResponseDto expectedTask = TASK_RESPONSE_DTO2;
        TaskResponseDto actualTask = taskService.removePerformer(taskId);
        assertEquals(expectedTask, actualTask);

    }

    @Test
    @SneakyThrows
    void updateStatus() {
        TaskResponseDto task = TASK_RESPONSE_DTO1;
        Long taskId = task.getId();
        TaskStatus taskStatus = TaskStatus.IN_PROGRESS;
        TaskResponseDto expectedTask = TASK_RESPONSE_DTO2;
        TaskResponseDto actualTask = taskService.updateTaskStatusById(taskId, taskStatus);
        assertEquals(expectedTask, actualTask);

    }

    @Test
    @SneakyThrows
    void cancelTask() {
        TaskResponseDto task = TASK_RESPONSE_DTO1;
        Long taskId = task.getId();
        assertTrue(taskRepository.findById(taskId).isPresent());
        taskService.cancelById(taskId);
        assertFalse(taskRepository.findById(taskId).isPresent());

        Long nonExistentTaskId = 999L;
        assertThrows(TaskNotFoundException.class, () -> taskService.
                cancelById(nonExistentTaskId));




    }
}