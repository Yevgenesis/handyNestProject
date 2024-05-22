package codezilla.handynestproject.controller;

import codezilla.handynestproject.HandyNestProjectApplication;
import codezilla.handynestproject.dto.task.TaskRequestDto;
import codezilla.handynestproject.dto.task.TaskResponseDto;
import codezilla.handynestproject.dto.task.TaskUpdateRequestDto;
import codezilla.handynestproject.model.enums.TaskStatus;
import codezilla.handynestproject.service.TaskService;
import codezilla.handynestproject.util.TestDatabaseConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
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
import static codezilla.handynestproject.service.TestData.TASK_RESPONSE_DTO5;
import static codezilla.handynestproject.service.TestData.TEST_ADDRESS_DTO1;
import static codezilla.handynestproject.service.TestData.TEST_PERFORMER2;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ContextConfiguration(classes = {TestDatabaseConfig.class, HandyNestProjectApplication.class})
@ExtendWith(SpringExtension.class)
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
    void findAllTest() {

       mockMvc.perform(get("/tasks"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.size()").value(5));
    }

    @Test
    @SneakyThrows
    void findByIdTest() {

        TaskResponseDto expectedTask = TASK_RESPONSE_DTO3;
        Long taskId = expectedTask.getId();

       mockMvc.perform(get("/tasks/{id}", taskId))
               .andExpect(status().isOk())
               .andExpect(content().json(objectMapper.writeValueAsString(expectedTask)));

        Long notExistingTaskId = 999L;
        mockMvc.perform(get("/tasks/{id}", notExistingTaskId))
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    void findAvailableTest() {

        List<TaskResponseDto> expectedTasks = List.of(TASK_RESPONSE_DTO1, TASK_RESPONSE_DTO4);
       mockMvc.perform(get("/tasks/open"))
               .andExpect(status().isOk())
               .andExpect(content().json(objectMapper.writeValueAsString(expectedTasks)));

    }

    @Test
    @SneakyThrows
    void getByUserIdTest() {

        List<TaskResponseDto> expectedTasks = List.of(TASK_RESPONSE_DTO4);
        Long userId = TASK_RESPONSE_DTO4.getUser().getId();
       mockMvc.perform(get("/tasks/user/{userId}", userId))
               .andExpect(status().isOk())
               .andExpect(content().json(objectMapper.writeValueAsString(expectedTasks)));

        Long notExistingUserId = 999L;
        mockMvc.perform(get("/tasks/user/{userId}", notExistingUserId))
                .andExpect(status().isNotFound());

    }

    @Test
    @SneakyThrows
    void findByPerformerIdTest() {
        List<TaskResponseDto> expectedTasks = List.of(TASK_RESPONSE_DTO5);
        Long id = TASK_RESPONSE_DTO5.getPerformer().getId();
        mockMvc.perform(get("/tasks/performer/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedTasks)));

        Long notExistingUserId = 999L;
        mockMvc.perform(get("/tasks/performer/{id}", notExistingUserId))
                .andExpect(status().isNotFound());

    }

    @Test
    @SneakyThrows
    void findByStatusTest() {

        List<TaskResponseDto> expectedTasks = List.of(TASK_RESPONSE_DTO3);
        TaskStatus taskStatus = TaskStatus.COMPLETED;
       mockMvc.perform(get("/tasks/status")
                       .param("status", taskStatus.name()))
               .andExpect(status().isOk())
               .andExpect(content().json(objectMapper.writeValueAsString(expectedTasks)));
    }

    @Test
    @SneakyThrows
    void createTest() {
        TaskRequestDto taskRequest = TASK_REQUEST_DTO1;
        TaskResponseDto expectedTask = TASK_RESPONSE_DTO1;
        expectedTask.setId(6L);
        Timestamp createdOn = Timestamp.valueOf(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
        expectedTask.setCreatedOn(createdOn);
        Timestamp updatedOn = Timestamp.valueOf(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
        expectedTask.setUpdatedOn(updatedOn);

        mockMvc.perform(MockMvcRequestBuilders
                .post("/tasks")
                .content(objectMapper.writeValueAsString(taskRequest)))

                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedTask)));

    }

    @Test
    @SneakyThrows
    void updateTest() {
        TaskUpdateRequestDto task = new TaskUpdateRequestDto(
                1L,
                "Починить кран",
                "Требуется починить кран на кухне",
                60.0,
                TEST_ADDRESS_DTO1,
                1L);

        TaskResponseDto expectedTask = TASK_RESPONSE_DTO1;
        expectedTask.setPrice(60.0);
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/tasks/update/{id}", expectedTask.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedTask)));
    }




    @Test
    @SneakyThrows
    void addPerformerTest() {
        TaskResponseDto task = TASK_RESPONSE_DTO1;
        Long taskId = task.getId();
        Long performerId = TEST_PERFORMER2.getId();

        mockMvc.perform(MockMvcRequestBuilders
                .put("/tasks/add/{TaskId}/{performerId}", taskId, performerId))
                .andExpect(status().isOk());

        Long notExistingPerformerId = 999L;
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/tasks/add/{TaskId}/{performerId}", taskId, notExistingPerformerId))
                .andExpect(status().isNotFound());

        Long notExistingTaskId = 999L;
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/tasks/add/{TaskId}/{performerId}", taskId, notExistingTaskId))
                .andExpect(status().isNotFound());

    }

    @Test
    @SneakyThrows
    void removePerformer() {
        TaskResponseDto task = TASK_RESPONSE_DTO2;
        Long taskId = task.getId();

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/tasks/removePerformer/{taskId}", taskId))
                .andExpect(status().isOk());

        Long notExistingTaskId = 999L;
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/tasks/removePerformer/{taskId}", notExistingTaskId))
                .andExpect(status().isNotFound());

    }

    @Test
    @SneakyThrows
    void updateStatus() {
        TaskResponseDto task = TASK_RESPONSE_DTO1;
        Long taskId = task.getId();
        TaskStatus taskStatus = TaskStatus.IN_PROGRESS;
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/tasks/updateStatus/{taskId}/{status}", taskId, taskStatus))
                .andExpect(status().isOk());

        Long notExistingTaskId = 999L;
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/tasks/removePerformer/{taskId}", notExistingTaskId))
                .andExpect(status().isNotFound());

    }

    @Test
    @SneakyThrows
    void cancelTask() {
        TaskResponseDto task = TASK_RESPONSE_DTO5;
        Long taskId = task.getId();
        mockMvc.perform(delete("/tasks/cancel/{taskId}", taskId))
                .andExpect(status().isOk());

        Long nonExistentTaskId = 999L;
       mockMvc.perform(delete("/tasks/cancel/{taskId}", nonExistentTaskId))
               .andExpect(status().isNotFound());


    }
}