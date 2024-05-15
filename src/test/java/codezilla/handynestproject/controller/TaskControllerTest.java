package codezilla.handynestproject.controller;

import codezilla.handynestproject.dto.task.TaskRequestDto;
import codezilla.handynestproject.dto.task.TaskResponseDto;
import codezilla.handynestproject.dto.task.TaskUpdateRequestDto;
import codezilla.handynestproject.model.enums.TaskStatus;
import codezilla.handynestproject.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static codezilla.handynestproject.service.TestData.ADDRESS_DTO;
import static codezilla.handynestproject.service.TestData.TASK_REQUEST_DTO;
import static codezilla.handynestproject.service.TestData.TASK_RESPONSE_DTO;
import static codezilla.handynestproject.service.TestData.TASK_RESPONSE_DTO_WITH_PERFORMER;
import static codezilla.handynestproject.service.TestData.TEST_PERFORMER2;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerTest {

    @MockBean
    private TaskService taskService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllTasksTest() throws Exception {

        List<TaskResponseDto> expectedTasks = List.of(TASK_RESPONSE_DTO_WITH_PERFORMER);
        when(taskService.getAllTasks()).thenReturn(expectedTasks);
        List<TaskResponseDto> actualTasks = taskService.getAllTasks();

        mockMvc.perform(get("/task"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", Matchers.hasSize(expectedTasks.size())));

        assertEquals("Tasks should be equal", expectedTasks, actualTasks);
    }

    @Test
    void getTaskByIdTest() throws Exception {

        TaskResponseDto expectedTask = TASK_RESPONSE_DTO;
        Long taskId = expectedTask.getId();
        when(taskService.getTaskById(anyLong())).thenReturn(expectedTask);
        TaskResponseDto actualTask = taskService.getTaskById(taskId);

        mockMvc.perform(get("/task/{id}", taskId))
                .andExpect(status().isOk());

        assertEquals("Task should be equal", expectedTask, actualTask);
    }

    @Test
    void getAvailableTasksTest() throws Exception {

        List<TaskResponseDto> ExpectedTasks = List.of(TASK_RESPONSE_DTO);
        when(taskService.getAvailableTasks()).thenReturn(ExpectedTasks);
        List<TaskResponseDto> actualTasks = taskService.getAvailableTasks();

        mockMvc.perform(get("/task/open"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(ExpectedTasks.size())));

        assertEquals("Tasks should be equal", ExpectedTasks, actualTasks);
    }

    @Test
    void getTasksByUserIdTest() throws Exception {

        List<TaskResponseDto> expectedTasks = List.of(TASK_RESPONSE_DTO);
        Long userId = TASK_RESPONSE_DTO.getId();
        when(taskService.getTasksByUserId(Mockito.anyLong())).thenReturn(expectedTasks);
        List<TaskResponseDto> actualTasks = taskService.getTasksByUserId(userId);

        mockMvc.perform(get("/task/byUser/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(expectedTasks.size())));

        assertEquals("Tasks should be equal", expectedTasks, actualTasks);
    }

    @Test
    void getTasksByPerformerId() throws Exception {

        List<TaskResponseDto> expectedTasks = List.of(TASK_RESPONSE_DTO_WITH_PERFORMER);
        Long performerId = TASK_RESPONSE_DTO_WITH_PERFORMER.getPerformer().getId();
        when(taskService.getTasksByPerformerId(Mockito.anyLong())).thenReturn(expectedTasks);
        List<TaskResponseDto> actualTasks = taskService.getTasksByPerformerId(performerId);

        mockMvc.perform(get("/task/byPerformer/{id}", performerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(expectedTasks.size())));

        assertEquals("Tasks should be equal", expectedTasks, actualTasks);
    }

    @Test
    void getTasksByStatus() throws Exception {

        List<TaskResponseDto> expectedTasks = List.of(TASK_RESPONSE_DTO_WITH_PERFORMER);
        TaskStatus taskStatus = TaskStatus.IN_PROGRESS;
        when(taskService.getTasksByStatus(taskStatus)).thenReturn(expectedTasks);
        List<TaskResponseDto> actualTasks = taskService.getTasksByStatus(taskStatus);

        mockMvc.perform(get("/task/status")
                        .param("status", taskStatus.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(expectedTasks.size())));

        assertEquals("Tasks should be equal", expectedTasks, actualTasks);
    }

    @Test
    void createTask() throws Exception {

        TaskRequestDto task = TASK_REQUEST_DTO;
        TaskResponseDto expectedTask = TASK_RESPONSE_DTO;
        when(taskService.createTask(task)).thenReturn(expectedTask);
        TaskResponseDto actualTask = taskService.createTask(task);

        mockMvc.perform(post("/task")
                        .content(objectMapper.writeValueAsString(task))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertEquals("created", expectedTask, actualTask);

    }

    @Test
    void createTaskByUserIdTest() throws Exception {

        TaskRequestDto task = TASK_REQUEST_DTO;
        Long userId = task.userId();
        TaskResponseDto expectedTask = TASK_RESPONSE_DTO;
        when(taskService.createTaskByUserId(userId, task)).thenReturn(expectedTask);
        TaskResponseDto actualTask = taskService.createTaskByUserId(userId, task);

        mockMvc.perform(post("/task/create/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedTask)));

        assertEquals("created", expectedTask, actualTask);
    }

    @Test
    void updateTaskTest() throws Exception {
        TaskUpdateRequestDto task = new TaskUpdateRequestDto(
                1L,
                "Test Title",
                "Test Description",
                0.0,
                ADDRESS_DTO,
                2L);
        Long taskId = task.getId();

        TaskResponseDto expectedTask = TASK_RESPONSE_DTO;
        when(taskService.updateTask(task)).thenReturn(expectedTask);
        TaskResponseDto actualTask = taskService.updateTask(task);

        mockMvc.perform(put("/task/update/{id}",taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isOk());

        assertEquals("updated", expectedTask, actualTask);

    }

    @Test
    void addPerformerToTaskTest() throws Exception {
        TaskResponseDto task = TASK_RESPONSE_DTO;
        Long taskId = task.getId();
        Long performerId = TEST_PERFORMER2.getId();
        TaskResponseDto expectedTask = TASK_RESPONSE_DTO_WITH_PERFORMER;
        when(taskService.addPerformerToTask(taskId, performerId)).thenReturn(expectedTask);
        TaskResponseDto actualTask = taskService.addPerformerToTask(taskId, performerId);

        mockMvc.perform(put("/task/add/{taskId}/{performerId}", taskId, performerId)
        .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isOk());

        assertEquals("added", expectedTask, actualTask);
    }

    @Test
    void removePerformerFromTask() throws Exception {
        TaskResponseDto task = TASK_RESPONSE_DTO_WITH_PERFORMER;
        Long taskId = task.getId();
        TaskResponseDto expectedTask = TASK_RESPONSE_DTO;
        when(taskService.removePerformerFromTask(taskId)).thenReturn(expectedTask);
        TaskResponseDto actualTask = taskService.removePerformerFromTask(taskId);

        mockMvc.perform(put("/task/removePerformer/{taskId}",taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isOk());

        assertEquals("removed", expectedTask, actualTask);

    }

    @Test
    void updateTaskStatus() throws Exception {
        TaskResponseDto task = TASK_RESPONSE_DTO;
        Long taskId = task.getId();
        TaskStatus taskStatus = TaskStatus.IN_PROGRESS;
        TaskResponseDto expectedTask = TASK_RESPONSE_DTO_WITH_PERFORMER;
        when(taskService.updateTaskStatusById(taskId, taskStatus)).thenReturn(expectedTask);
        TaskResponseDto actualTask = taskService.updateTaskStatusById(taskId, taskStatus);

        mockMvc.perform(put("/task/updateStatus/{taskId}/{status}", taskId, taskStatus)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isOk());

        assertEquals("updated", expectedTask, actualTask);
    }

    @Test
    void deleteTask() throws Exception {
        TaskResponseDto task = TASK_RESPONSE_DTO;
        Long taskId = task.getId();

        mockMvc.perform(delete("/task/delete/{id}", taskId))
                .andExpect(status().isOk());

        verify(taskService).deleteTaskById(taskId);

    }
}