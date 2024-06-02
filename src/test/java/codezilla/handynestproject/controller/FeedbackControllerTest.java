package codezilla.handynestproject.controller;

import codezilla.handynestproject.HandyNestProjectApplication;
import codezilla.handynestproject.dto.feedback.FeedbackCreateRequestDto;
import codezilla.handynestproject.dto.feedback.FeedbackResponseDto;
import codezilla.handynestproject.service.FeedbackService;
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
import java.sql.Timestamp;

import static codezilla.handynestproject.testData.UserTestData.USER_NESTED_RESPONSE_DTO1;
import static codezilla.handynestproject.testData.UserTestData.USER_NESTED_RESPONSE_DTO3;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(classes = {TestDatabaseConfig.class, HandyNestProjectApplication.class})
class FeedbackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    @Transactional
    @SneakyThrows
    void addTest() {
        FeedbackCreateRequestDto requestDto = FeedbackCreateRequestDto.builder()
                .senderId(1L)
                .text("feedback test")
                .taskId(5L)
                .grade(4L)
                .build();

        FeedbackResponseDto expected = FeedbackResponseDto.builder()
                .id(7L)
                .sender(USER_NESTED_RESPONSE_DTO1)
                .text("feedback test")
                .taskId(5L)
                .grade(4L)
                .createdOn(null)
                .build();

        var result = mockMvc.perform(MockMvcRequestBuilders
                        .post("/feedbacks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .characterEncoding("UTF-8"))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        FeedbackResponseDto actual = objectMapper.readValue(jsonResponse, FeedbackResponseDto.class);
        assertNotNull(actual.getId());
        assertEquals(actual.getSender().getId(), expected.getSender().getId());
        assertEquals(actual.getTaskId(), expected.getTaskId());
    }

    @Test
    @Transactional
    @SneakyThrows
    void findAllTest() {
        mockMvc.perform(get("/feedbacks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(12));

    }

    @Test
    @Transactional
    @SneakyThrows
    void findByIdTest() {
        Long id = 1L;
        FeedbackResponseDto expected = FeedbackResponseDto.builder()
                .id(id)
                .taskId(4L)
                .sender(USER_NESTED_RESPONSE_DTO3)
                .text("Хреновый заказчик! Не угостил чаем")
                .grade(3L)
                .createdOn(Timestamp.valueOf("2024-05-14 12:00:00"))
                .build();

        mockMvc.perform(get("/feedbacks/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expected)));
    }

    @Test
    @SneakyThrows
    void findByIdExistingId() {
        Long notExistingId = 999L;
        mockMvc.perform(get("/feedbacks/{id}", notExistingId))
                .andExpect(status().isNotFound());
    }


    @Test
    @Transactional
    @SneakyThrows
    void findByTaskId() {
        Long taskId = 3L;
        mockMvc.perform(get("/feedbacks/task/{taskId}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }

    @Test
    @SneakyThrows
    void findByTaskIdExistingTaskId() {
        Long notExistingTaskId = 999L;
        mockMvc.perform(get("/feedbacks/task/{taskId}", notExistingTaskId))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    @SneakyThrows
    void findReceivedByUserId() {
        Long userId = 3L;
        mockMvc.perform(get("/feedbacks/user/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    @SneakyThrows
    void findReceivedByUserIdExistingUserId() {
        Long notExistingUserId = 999L;
        mockMvc.perform(get("/feedbacks/user/{userId}", notExistingUserId))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    @SneakyThrows
    void findReceivedForPerformerId() {
        Long performerId = 2L;
        mockMvc.perform(get("/feedbacks/performer/{senderId}", performerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @SneakyThrows
    void findReceivedForPerformerIdExistingPerformerId() {
        Long notExistingPerformerId = 999L;
        mockMvc.perform(get("/feedbacks/performer/{senderId}", notExistingPerformerId))
                .andExpect(status().isNotFound());
    }


}