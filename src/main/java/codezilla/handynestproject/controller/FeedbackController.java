package codezilla.handynestproject.controller;

import codezilla.handynestproject.dto.feedback.FeedbackCreateRequestDto;
import codezilla.handynestproject.dto.feedback.FeedbackResponseDto;
import codezilla.handynestproject.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;


    //POST
    @PostMapping
    public FeedbackResponseDto add(@RequestBody @Valid FeedbackCreateRequestDto requestDto) {
        return feedbackService.add(requestDto);
    }

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

    // Достать все фитбеки полученные конкретным юзером
    @GetMapping("/user/{userId}")
    public List<FeedbackResponseDto> findReceivedForUserId(@PathVariable Long userId) {
        return feedbackService.findAllForUserId(userId);
    }

    // Достать все фитбеки полученные конкретным юзером
    @GetMapping("/current")
    public List<FeedbackResponseDto> findReceivedForCurrentUser() {
        return feedbackService.findAllForCurrentUser();
    }

    // Достать все фитбеки полученные конкретным перформером
    @GetMapping("/performer/{performerId}")
    public List<FeedbackResponseDto> findReceivedForPerformerId(@PathVariable Long performerId) {
        return feedbackService.findAllForPerformerId(performerId);
    }

}
