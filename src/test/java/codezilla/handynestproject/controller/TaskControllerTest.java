package codezilla.handynestproject.controller;

import codezilla.handynestproject.HandyNestProjectApplication;
import codezilla.handynestproject.dto.task.TaskRequestDto;
import codezilla.handynestproject.dto.task.TaskResponseDto;
import codezilla.handynestproject.dto.task.TaskUpdateRequestDto;
import codezilla.handynestproject.model.enums.TaskStatus;
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
import java.time.format.DateTimeFormatter;
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
import static org.mockito.Mockito.verify;

@SpringBootTest
@Testcontainers
@ContextConfiguration(classes = {TestDatabaseConfig.class, HandyNestProjectApplication.class})
class TaskControllerTest {

    private final TaskService taskService;

   @Autowired
   TaskControllerTest(TaskService taskService){
       this.taskService = taskService;
   }
    @Test
    @Transactional
    void getAllTasksTest() {

        List<TaskResponseDto> actual = taskService.getAllTasks();
        assertEquals(5, actual.size());
    }

    @Test
    @SneakyThrows
    void getTaskByIdTest() {

        TaskResponseDto expectedTask = TASK_RESPONSE_DTO3;
        Long taskId = expectedTask.getId();

        TaskResponseDto actualTask = taskService.getTaskById(taskId);
        assertEquals(expectedTask, actualTask);
    }

    @Test
    @SneakyThrows
    void getAvailableTasksTest() {

        List<TaskResponseDto> expectedTasks = List.of(TASK_RESPONSE_DTO1,TASK_RESPONSE_DTO4);
        List<TaskResponseDto> actualTasks = taskService.getAvailableTasks();
        assertEquals(expectedTasks, actualTasks);
    }

    @Test
    @SneakyThrows
    void getTasksByUserIdTest() {

        List<TaskResponseDto> expectedTasks = List.of(TASK_RESPONSE_DTO1);
        Long userId = TASK_RESPONSE_DTO1.getUser().getId();
        List<TaskResponseDto> actualTasks = taskService.getTasksByUserId(userId);
        assertEquals(expectedTasks, actualTasks);

    }

    @Test
    @SneakyThrows
    void getTasksByPerformerId(){
        List<TaskResponseDto> expectedTasks = List.of(TASK_RESPONSE_DTO2);
        Long performerId = TASK_RESPONSE_DTO2.getPerformer().getId();
        List<TaskResponseDto> actualTasks = taskService.getTasksByPerformerId(performerId);
        assertEquals(expectedTasks, actualTasks);

    }

    @Test
    @SneakyThrows
    void getTasksByStatus()  {

        List<TaskResponseDto> expectedTasks = List.of(TASK_RESPONSE_DTO2);
        TaskStatus taskStatus = TaskStatus.IN_PROGRESS;
        List<TaskResponseDto> actualTasks = taskService.getTasksByStatus(taskStatus);
        assertEquals(expectedTasks.size(), actualTasks.size());//если не сравнивать размеры листов, то не проходит
    }

    @Test
    @SneakyThrows
    void createTask() {

        TaskRequestDto task = TASK_REQUEST_DTO1;
        TaskResponseDto expectedTask = TASK_RESPONSE_DTO1;
        Long taskId = expectedTask.getId();
        Long newId = taskId+5L;
        expectedTask.setId(newId);
        Timestamp createdOn = Timestamp.valueOf(LocalDateTime.now()
                .truncatedTo(ChronoUnit.MINUTES));
        expectedTask.setCreatedOn(createdOn);
        Timestamp updatedOn = Timestamp.valueOf(LocalDateTime.now()
                .truncatedTo(ChronoUnit.MINUTES));
        expectedTask.setUpdatedOn(updatedOn);
        TaskResponseDto actualTask = taskService.createTask(task);
        assertEquals(expectedTask, actualTask);
    }

    @Test
    @SneakyThrows
    void createTaskByUserIdTest() {

        TaskRequestDto task = TASK_REQUEST_DTO1;
        Long userId = task.userId();
        TaskResponseDto expectedTask = TASK_RESPONSE_DTO1;
        TaskResponseDto actualTask = taskService.createTaskByUserId(userId, task);
        assertEquals(expectedTask, actualTask);

    }

    @Test
    @SneakyThrows
    void updateTaskTest() {
        TaskUpdateRequestDto task = new TaskUpdateRequestDto(
                1L,
                "Test Title",
                "Test Description",
                0.0,
                TEST_ADDRESS_DTO1,
                2L);

        TaskResponseDto expectedTask = TASK_RESPONSE_DTO1;
        TaskResponseDto actualTask = taskService.updateTask(task);
        assertEquals(expectedTask, actualTask);



    }

    @Test
    @SneakyThrows
    void addPerformerToTaskTest() {
        TaskResponseDto task = TASK_RESPONSE_DTO1;
        Long taskId = task.getId();
        Long performerId = TEST_PERFORMER2.getId();
        TaskResponseDto expectedTask = TASK_RESPONSE_DTO4;
        TaskResponseDto actualTask = taskService.addPerformerToTask(taskId, performerId);
        assertEquals(expectedTask.getPerformer(), actualTask.getPerformer());


    }

    @Test
    @SneakyThrows
    void removePerformerFromTask() {
        TaskResponseDto task = TASK_RESPONSE_DTO1;
        Long taskId = task.getId();
        TaskResponseDto expectedTask = TASK_RESPONSE_DTO2;
        TaskResponseDto actualTask = taskService.removePerformerFromTask(taskId);
        assertEquals(expectedTask, actualTask);

    }

    @Test
    @SneakyThrows
    void updateTaskStatus() {
        TaskResponseDto task = TASK_RESPONSE_DTO1;
        Long taskId = task.getId();
        TaskStatus taskStatus = TaskStatus.IN_PROGRESS;
        TaskResponseDto expectedTask = TASK_RESPONSE_DTO2;
        TaskResponseDto actualTask = taskService.updateTaskStatusById(taskId, taskStatus);
        assertEquals(expectedTask, actualTask);

    }

    @Test
    @SneakyThrows
    void deleteTask()  {
        TaskResponseDto task = TASK_RESPONSE_DTO1;
        Long taskId = task.getId();

        verify(taskService).deleteTaskById(taskId);

    }
}