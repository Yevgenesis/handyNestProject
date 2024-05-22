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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static codezilla.handynestproject.service.TestData.FEEDBACK_REQUEST_DTO2;
import static codezilla.handynestproject.service.TestData.FEEDBACK_RESPONSE_DTO1;
import static codezilla.handynestproject.service.TestData.FEEDBACK_RESPONSE_DTO2;
import static codezilla.handynestproject.service.TestData.FEEDBACK_RESPONSE_DTO3;
import static codezilla.handynestproject.service.TestData.FEEDBACK_RESPONSE_DTO5;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ContextConfiguration(classes = {TestDatabaseConfig.class, HandyNestProjectApplication.class})
class FeedbackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    @SneakyThrows
    void findAllTest() {
        mockMvc.perform(get("/feedbacks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(6));

    }

    @Test
    @SneakyThrows
    void findByIdTest() {
        FeedbackResponseDto expected = FEEDBACK_RESPONSE_DTO1;
        Long id = expected.getId();

        mockMvc.perform(get("/feedbacks/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expected)));
//
//        Long notExistingId = 999L;
//        mockMvc.perform(get("/feedbacks/{id}", notExistingId))
//                .andExpect(status().isNotFound());

    }

    @Test
    @SneakyThrows
    void findByTaskId() {
        List<FeedbackResponseDto> expected = List.of(FEEDBACK_RESPONSE_DTO3);
        Long taskId = FEEDBACK_RESPONSE_DTO3.getTaskId();

    }

    @Test
    @SneakyThrows
    void getByUserId() {
        List<FeedbackResponseDto> expected = List.of(FEEDBACK_RESPONSE_DTO5);
        Long userId = FEEDBACK_RESPONSE_DTO5.getSender().getId();

    }

    //FeedbackErrorException: You can't send feedback more than once for this task
    @Test
    @SneakyThrows
    void addTest() {
        FeedbackCreateRequestDto requestDto = FEEDBACK_REQUEST_DTO2;
        FeedbackResponseDto expected = FEEDBACK_RESPONSE_DTO2;
        expected.setId(6L);


    }

}