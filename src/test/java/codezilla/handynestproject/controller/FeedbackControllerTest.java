package codezilla.handynestproject.controller;

import codezilla.handynestproject.HandyNestProjectApplication;
import codezilla.handynestproject.dto.feedback.FeedbackResponseDto;
import codezilla.handynestproject.service.FeedbackService;
import codezilla.handynestproject.util.TestDatabaseConfig;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static codezilla.handynestproject.service.TestData.FEEDBACK_RESPONSE_DTO5;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
        void getAllFeedbackTest() {
        List<FeedbackResponseDto> actual = feedbackService.getAllFeedback();
        assertEquals(5, actual.size());

    }

    @Test
    void getFeedbackTest() throws Exception {
        FeedbackResponseDto expected = FEEDBACK_RESPONSE_DTO5;
        Long id = expected.getId();
        FeedbackResponseDto actual = feedbackService.getFeedbackById(id);
        assertEquals(expected, actual);
    }
}