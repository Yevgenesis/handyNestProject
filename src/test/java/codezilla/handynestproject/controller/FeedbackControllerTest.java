package codezilla.handynestproject.controller;

import codezilla.handynestproject.HandyNestProjectApplication;
import codezilla.handynestproject.dto.feedback.FeedbackCreateRequestDto;
import codezilla.handynestproject.dto.feedback.FeedbackResponseDto;
import codezilla.handynestproject.exception.FeedbackNotFoundException;
import codezilla.handynestproject.exception.TaskNotFoundException;
import codezilla.handynestproject.service.FeedbackService;
import codezilla.handynestproject.util.TestDatabaseConfig;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static codezilla.handynestproject.service.TestData.FEEDBACK_REQUEST_DTO2;
import static codezilla.handynestproject.service.TestData.FEEDBACK_RESPONSE_DTO2;
import static codezilla.handynestproject.service.TestData.FEEDBACK_RESPONSE_DTO3;
import static codezilla.handynestproject.service.TestData.FEEDBACK_RESPONSE_DTO5;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Testcontainers
@ContextConfiguration(classes = {TestDatabaseConfig.class, HandyNestProjectApplication.class})
class FeedbackControllerTest {


    private final FeedbackService feedbackService;

    @Autowired
   FeedbackControllerTest(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @Test
    @SneakyThrows
        void getAllTest() {
        List<FeedbackResponseDto> actual = feedbackService.getAllFeedback();
        assertEquals(5, actual.size());

    }

    @Test
    @SneakyThrows
    void getByIdTest() {
        FeedbackResponseDto expected = FEEDBACK_RESPONSE_DTO5;
        Long id = expected.getId();
        FeedbackResponseDto actual = feedbackService.getFeedbackById(id);
        assertEquals(expected, actual);

        Long notExistingId = 999L;
        assertThrows(FeedbackNotFoundException.class, () -> feedbackService
                .getFeedbackById(notExistingId));
    }

    @Test
    @SneakyThrows
    void getByTaskId(){
        List<FeedbackResponseDto> expected = List.of(FEEDBACK_RESPONSE_DTO3);
        Long taskId = FEEDBACK_RESPONSE_DTO3.getTaskId();
        List<FeedbackResponseDto> actual = feedbackService.getFeedbackByTaskId(taskId);
        assertEquals(expected, actual);
    }

    @Test
    @SneakyThrows
    void getByUserId(){
        List<FeedbackResponseDto> expected = List.of(FEEDBACK_RESPONSE_DTO5);
        Long userId = FEEDBACK_RESPONSE_DTO5.getSender().getId();
        List<FeedbackResponseDto> actual = feedbackService.getFeedbackBySenderId(userId);
        assertEquals(expected, actual);
    }
    //FeedbackErrorException: You can't send feedback more than once for this task
    @Test
    @SneakyThrows
    void addTest() {
        FeedbackCreateRequestDto requestDto = FEEDBACK_REQUEST_DTO2;
        FeedbackResponseDto expected = FEEDBACK_RESPONSE_DTO2;
        expected.setId(6L);
        FeedbackResponseDto actual = feedbackService.addFeedback(requestDto);
        assertEquals(expected, actual);

    }

}