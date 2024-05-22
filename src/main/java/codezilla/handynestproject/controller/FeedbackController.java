package codezilla.handynestproject.controller;

import codezilla.handynestproject.dto.feedback.FeedbackCreateRequestDto;
import codezilla.handynestproject.dto.feedback.FeedbackResponseDto;
import codezilla.handynestproject.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;


    //GET
    @GetMapping
    public List<FeedbackResponseDto> findAll() {
        return feedbackService.findAll();
    }

    @GetMapping("/{id}")
    public FeedbackResponseDto findById(@PathVariable Long id) {
        return feedbackService.findById(id);
    }

    @GetMapping("/task/{taskId}")
    public List<FeedbackResponseDto> findByTaskID(@PathVariable Long taskId) {
        return feedbackService.findByTaskId(taskId);
    }

    // ToDo сделать правильный запрос в базу
    @GetMapping("/user/{senderId}")
    public List<FeedbackResponseDto> findByUserID(@PathVariable Long senderId) {
        return feedbackService.findBySenderId(senderId);
    }

    // ToDo сделать правильный запрос в базу
    @GetMapping("/performer/{senderId}")
    public List<FeedbackResponseDto> findByPerformerID(@PathVariable Long senderId) {
        return feedbackService.findBySenderId(senderId);
    }

    //POST
    @PostMapping
    public FeedbackResponseDto addFeedback(@RequestBody FeedbackCreateRequestDto requestDto) {
        return feedbackService.add(requestDto);

    }

}
