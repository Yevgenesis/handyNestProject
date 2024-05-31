package codezilla.handynestproject.controller;

import codezilla.handynestproject.HandyNestProjectApplication;
import codezilla.handynestproject.dto.task.TaskResponseDto;
import codezilla.handynestproject.dto.task.TaskUpdateRequestDto;
import codezilla.handynestproject.model.enums.TaskStatus;
import codezilla.handynestproject.service.TaskService;
import codezilla.handynestproject.util.TestDatabaseConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;

import static codezilla.handynestproject.testData.AddressTestData.TEST_ADDRESS_DTO1;
import static codezilla.handynestproject.testData.PerformerTestData.TEST_PERFORMER2;
import static codezilla.handynestproject.testData.TaskTestData.TASK_REQUEST_DTO1;
import static codezilla.handynestproject.testData.TaskTestData.TASK_RESPONSE_DTO1;
import static codezilla.handynestproject.testData.TaskTestData.TASK_RESPONSE_DTO3;
import static codezilla.handynestproject.testData.TaskTestData.TASK_RESPONSE_DTO5;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(classes = {TestDatabaseConfig.class, HandyNestProjectApplication.class})
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskService taskService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Transactional
    @SneakyThrows
    void createTest() {
        TaskResponseDto expected = TASK_RESPONSE_DTO1;
        var result = mockMvc.perform(MockMvcRequestBuilders
                        .post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TASK_REQUEST_DTO1))
                        .characterEncoding("UTF-8"))
                .andExpect(status().isOk())
                .andReturn();


        String jsonResponse = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        TaskResponseDto actualTask = objectMapper.readValue(jsonResponse, TaskResponseDto.class);

        assertNotNull(actualTask.getId());
        assertEquals(expected.getTitle(), actualTask.getTitle());
        assertEquals(expected.getDescription(), actualTask.getDescription());
        assertEquals(expected.getPrice(), actualTask.getPrice());
        assertEquals(expected.getAddress(), actualTask.getAddress());
        assertEquals(expected.getUser(), actualTask.getUser());
        assertEquals(expected.getWorkingTime(), actualTask.getWorkingTime());
        assertEquals(expected.getCategory(), actualTask.getCategory());

        // Проверка временных полей с допустимой погрешностью
        assertTrue(Math.abs(actualTask.getCreatedOn().getTime() - System.currentTimeMillis()) < 1000);
        assertTrue(Math.abs(actualTask.getUpdatedOn().getTime() - System.currentTimeMillis()) < 1000);
    }

    @Test
    @Transactional
    @SneakyThrows
    void findAllTest() {
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(15));
    }

    @Test
    @SneakyThrows
    void findByIdTest() {
        TaskResponseDto expectedTask = TASK_RESPONSE_DTO3;
        mockMvc.perform(get("/tasks/{id}", expectedTask.getId()))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedTask)));
    }

    @Test
    @SneakyThrows
    void findByIdExistingTaskIdTest() {
        Long notExistingTaskId = 999L;
        mockMvc.perform(get("/tasks/{id}", notExistingTaskId))
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    void findAvailableTest() {
        mockMvc.perform(get("/tasks/open"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(3));
    }

    @Test
    @SneakyThrows
    void findByUserIdTest() {
        Long userId = 5L;
        mockMvc.perform(get("/tasks/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2));
    }

    @Test
    @SneakyThrows
    void findByUserIdExistingUserIdTest() {
        Long notExistingUserId = 999L;
        mockMvc.perform(get("/tasks/user/{userId}", notExistingUserId))
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    void findByPerformerIdTest() {
        Long userId = 3L;
        mockMvc.perform(get("/tasks/performer/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(6));
    }

    @Test
    @SneakyThrows
    void findByPerformerIdExistingUserIdTest() {
        Long notExistingUserId = 999L;
        mockMvc.perform(get("/tasks/performer/{id}", notExistingUserId))
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    void findByStatusTest() {
        TaskStatus status = TaskStatus.COMPLETED;
        mockMvc.perform(get("/tasks/status/{status}", status))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(8));
    }

    @Test
    @SneakyThrows
    void findByUnrefereedByUserIdTest() {
        Long userId = 5L;
        mockMvc.perform(get("/tasks/user/{userId}/unrefereed", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }

    @Test
    @SneakyThrows
    void findByUnrefereedByUserIdExistingUserIdTest() {
        Long notExistingUserId = 999L;
        mockMvc.perform(get("/tasks/user/{userId}/unrefereed", notExistingUserId))
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    void findByUnrefereedByPerformerIdTest() {
        Long performerId = 3L;
        mockMvc.perform(get("/tasks/performer/{performerId}/unrefereed", performerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(4));
    }

    @Test
    @SneakyThrows
    void findByUnrefereedByPerformerIdExistingPerformerIdTest() {
        Long notExistingPerformerId = 999L;
        mockMvc.perform(get("/tasks/performer/{performerId}/unrefereed", notExistingPerformerId))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    @SneakyThrows
    void updateTest() {
        TaskUpdateRequestDto task = new TaskUpdateRequestDto(
                1L,
                "Починить кран",
                "Требуется починить кран на кухне",
                50.0,
                TEST_ADDRESS_DTO1,
                1L);

        TaskResponseDto expectedTask = TASK_RESPONSE_DTO1;

        var result = mockMvc.perform(MockMvcRequestBuilders
                        .put("/tasks/update/{id}", expectedTask.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task))
                        .characterEncoding("UTF-8"))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        TaskResponseDto actualTask = objectMapper.readValue(jsonResponse, TaskResponseDto.class);

        assertEquals(expectedTask.getId(), actualTask.getId());
        assertEquals(expectedTask.getTitle(), actualTask.getTitle());
        assertEquals(expectedTask.getDescription(), actualTask.getDescription());
        assertEquals(expectedTask.getPrice(), actualTask.getPrice());
        assertEquals(expectedTask.getAddress(), actualTask.getAddress());
        assertEquals(expectedTask.getUser(), actualTask.getUser());

        // Проверка временных полей с допустимой погрешностью
        assertTrue(Math.abs(actualTask.getUpdatedOn().getTime() - System.currentTimeMillis()) < 1000);
    }

    @Test
    @Transactional
    @SneakyThrows
    void addPerformerTest() {
        TaskResponseDto task = TASK_RESPONSE_DTO1;
        Long taskId = task.getId();
        Long performerId = TEST_PERFORMER2.getId();

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/tasks/{TaskId}/addPerformer/{performerId}", taskId, performerId))
                .andExpect(status().isOk());
    }

    @Test
    @Transactional
    @SneakyThrows
    void addPerformerExistingTaskIdTest() {
        Long notExistingTaskId = 999L;
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/tasks/{TaskId}/addPerformer/{performerId}", notExistingTaskId, 2L))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    @SneakyThrows
    void addPerformerExistingPerformerIdTest() {

        Long notExistingPerformerId = 999L;
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/tasks/{TaskId}/addPerformer/{performerId}", 1L, notExistingPerformerId))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    @SneakyThrows
    void removePerformerTest() {
        TaskResponseDto task = TASK_RESPONSE_DTO3;
        Long taskId = task.getId();

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/tasks/{taskId}/removePerformer", taskId))
                .andExpect(status().isOk());
    }

    @Test
    @Transactional
    @SneakyThrows
    void removePerformerExistingTaskIdTest() {
        Long notExistingTaskId = 999L;
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/tasks/{taskId}/removePerformer", notExistingTaskId))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    @SneakyThrows
    void updateStatusTest() {
        TaskResponseDto task = TASK_RESPONSE_DTO1;
        Long taskId = task.getId();
        TaskStatus taskStatus = TaskStatus.IN_PROGRESS;
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/tasks/{taskId}/status//{status}", taskId, taskStatus))
                .andExpect(status().isOk());
    }

    @Test
    @Transactional
    @SneakyThrows
    void updateStatusExistingTaskIdTest() {

        Long notExistingTaskId = 999L;
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/tasks/{taskId}/status//{status}", notExistingTaskId, TaskStatus.IN_PROGRESS))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    @SneakyThrows
    void updateStatusExistingTaskStatusTest() {
        TaskStatus notExistingTaskStatus = TaskStatus.CANCELED;
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/tasks/{taskId}/status//{status}", 3L, notExistingTaskStatus))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    @SneakyThrows
    void cancel() {
        TaskResponseDto task = TASK_RESPONSE_DTO5;
        Long taskId = task.getId();
        mockMvc.perform(delete("/tasks/cancel/{taskId}", taskId))
                .andExpect(status().isOk());
    }

    @Test
    @Transactional
    @SneakyThrows
    void cancelExistentTaskIdTest() {

        Long nonExistentTaskId = 999L;
        mockMvc.perform(delete("/tasks/cancel/{taskId}", nonExistentTaskId))
                .andExpect(status().isNotFound());
    }

}