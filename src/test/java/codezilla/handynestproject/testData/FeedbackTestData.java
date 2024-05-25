package codezilla.handynestproject.testData;

import codezilla.handynestproject.dto.feedback.FeedbackCreateRequestDto;
import codezilla.handynestproject.dto.feedback.FeedbackResponseDto;
import codezilla.handynestproject.model.entity.Feedback;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static codezilla.handynestproject.testData.TaskTestData.TEST_TASK2;
import static codezilla.handynestproject.testData.TaskTestData.TEST_TASK3;
import static codezilla.handynestproject.testData.TaskTestData.TEST_TASK4;
import static codezilla.handynestproject.testData.TaskTestData.TEST_TASK5;
import static codezilla.handynestproject.testData.UserTestData.TEST_USER2;
import static codezilla.handynestproject.testData.UserTestData.TEST_USER3;
import static codezilla.handynestproject.testData.UserTestData.TEST_USER4;
import static codezilla.handynestproject.testData.UserTestData.TEST_USER5;
import static codezilla.handynestproject.testData.UserTestData.USER_NESTED_RESPONSE_DTO1;
import static codezilla.handynestproject.testData.UserTestData.USER_NESTED_RESPONSE_DTO2;
import static codezilla.handynestproject.testData.UserTestData.USER_NESTED_RESPONSE_DTO3;
import static codezilla.handynestproject.testData.UserTestData.USER_NESTED_RESPONSE_DTO4;
import static codezilla.handynestproject.testData.UserTestData.USER_NESTED_RESPONSE_DTO5;

public class FeedbackTestData {
    /**
     * Feedback
     */

    public static final Feedback TEST_FEEDBACK1 = Feedback.builder()
            .id(1L)
            .text("Хреновый заказчик! Не угостил чаем")
            .sender(TEST_USER3)
            .grade(3L)
            .task(TEST_TASK4)
            .createdOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 5, 14), LocalTime.of(12, 0, 0))))

            .build();
    public static final Feedback TEST_FEEDBACK2 = Feedback.builder()
            .id(2L)
            .text("Заказчица бомба!")
            .sender(TEST_USER2)
            .grade(4L)
            .task(TEST_TASK5)
            .createdOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 5, 14), LocalTime.of(12, 15, 0))))
            .build();

    public static final Feedback TEST_FEEDBACK3 = Feedback.builder()
            .id(3L)
            .sender(TEST_USER4)
            .text(null)
            .grade(3L)
            .task(TEST_TASK2)
            .createdOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 5, 14), LocalTime.of(12, 30, 0))))
            .build();

    public static final Feedback TEST_FEEDBACK4 = Feedback.builder()
            .id(4L)
            .sender(TEST_USER5)
            .text("Отличный заказчик!")
            .grade(5L)
            .task(TEST_TASK3)
            .createdOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 5, 14), LocalTime.of(12, 30, 0))))
            .build();

    public static final Feedback TEST_FEEDBACK5 = Feedback.builder()
            .id(5L)
            .sender(TEST_USER3)
            .text("Требуется улучшение")
            .grade(1L)
            .task(TEST_TASK2)
            .createdOn(Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 5, 14), LocalTime.of(13, 0, 0))))
            .build();

    /**
     * FeedbackCreateRequestDto
     */

    public static final FeedbackCreateRequestDto FEEDBACK_REQUEST_DTO1 = new FeedbackCreateRequestDto(
            3L, "Хреновый заказчик! Не угостил чаем", 3L, 4L);

    public static final FeedbackCreateRequestDto FEEDBACK_REQUEST_DTO2 = new FeedbackCreateRequestDto(
            2L, "Заказчица бомба!", 4L, 5L);

    public static final FeedbackCreateRequestDto FEEDBACK_REQUEST_DTO3 = new FeedbackCreateRequestDto(
            4L, null, 3L, 2L);

    public static final FeedbackCreateRequestDto FEEDBACK_REQUEST_DTO4 = new FeedbackCreateRequestDto(
            5L, "Отличный заказчик!", 5L, 3L);

    public static final FeedbackCreateRequestDto FEEDBACK_REQUEST_DTO5 = new FeedbackCreateRequestDto(
            3L, "Требуется улучшение", 1L, 2L);

    public static final FeedbackCreateRequestDto FEEDBACK_REQUEST_DTO6 = new FeedbackCreateRequestDto(
            TEST_USER2.getId(), "Ворюга! Украл пачку чая!", 1L, 4L);

    /**
     * FeedbackResponseDto
     */

    public static final FeedbackResponseDto FEEDBACK_RESPONSE_DTO1 = new FeedbackResponseDto(
            1L,
            USER_NESTED_RESPONSE_DTO3,
            "Хреновый заказчик! Не угостил чаем",
            3L,
            4L,
            Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                    (2024, 5, 14), LocalTime.of(12, 0, 0))));

    public static final FeedbackResponseDto FEEDBACK_RESPONSE_DTO2 = new FeedbackResponseDto(
            2L, USER_NESTED_RESPONSE_DTO2, "Заказчица бомба!", 4L,
            TEST_TASK5.getId(), Timestamp.valueOf(LocalDateTime.of(LocalDate.of
            (2024, 5, 14), LocalTime.of(12, 15, 0))));

    public static final FeedbackResponseDto FEEDBACK_RESPONSE_DTO3 = new FeedbackResponseDto(
            3L, USER_NESTED_RESPONSE_DTO4, null, 3L,
            2L, Timestamp.valueOf(LocalDateTime.of(LocalDate.of
            (2024, 5, 14), LocalTime.of(12, 30, 0))));

    public static final FeedbackResponseDto FEEDBACK_RESPONSE_DTO4 = new FeedbackResponseDto(
            4L, USER_NESTED_RESPONSE_DTO5, "Отличный заказчик!", 5L,
            3L, Timestamp.valueOf(LocalDateTime.of(LocalDate.of
            (2024, 5, 14), LocalTime.of(12, 30, 0))));

    public static final FeedbackResponseDto FEEDBACK_RESPONSE_DTO5 = new FeedbackResponseDto(
            5L, USER_NESTED_RESPONSE_DTO3, "Требуется улучшение", 1L,
            2L, Timestamp.valueOf(LocalDateTime.of(LocalDate.of
            (2024, 5, 14), LocalTime.of(13, 0, 0))));

    public static final FeedbackResponseDto FEEDBACK_RESPONSE_DTO6 = new FeedbackResponseDto(
            6L, USER_NESTED_RESPONSE_DTO2, "Ворюга! Украл пачку чая!", 1L,
            4L, Timestamp.valueOf(LocalDateTime.of(LocalDate.of
            (2024, 5, 14), LocalTime.of(12, 30, 0))));
}
