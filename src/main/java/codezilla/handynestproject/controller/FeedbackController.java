package codezilla.handynestproject.controller;

import codezilla.handynestproject.dto.feedback.FeedbackResponseDto;
import codezilla.handynestproject.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;


    @GetMapping
    public List<FeedbackResponseDto> getAllFeedback() {
        return feedbackService.getAllFeedback();
    }

    @GetMapping("/{id}")
    public FeedbackResponseDto getFeedbackById(@PathVariable Long id) {
        return feedbackService.getFeedbackById(id);
    }

    // ToDo исправить scope
    @GetMapping("/task/{taskId}")
    public List<FeedbackResponseDto> getFeedbacksByTaskID(@PathVariable Long taskId) {
        return feedbackService.getFeedbackByTaskId(taskId);
    }


    @GetMapping("/sender/{senderId}")
    public List<FeedbackResponseDto> getFeedbacksByUserID(@PathVariable Long senderId) {
        return feedbackService.getFeedbackBySenderId(senderId);
    }

}
