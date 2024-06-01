package codezilla.handynestproject.controller;

import codezilla.handynestproject.dto.feedback.FeedbackCreateRequestDto;
import codezilla.handynestproject.dto.feedback.FeedbackResponseDto;
import codezilla.handynestproject.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAnyAuthority('USER','PERFORMER')")
    @PostMapping
    public FeedbackResponseDto add(@RequestBody @Valid FeedbackCreateRequestDto requestDto) {
        return feedbackService.add(requestDto);
    }

    //GET
    @PreAuthorize("hasAnyAuthority('USER','PERFORMER','ADMIN')")
    @GetMapping
    public List<FeedbackResponseDto> findAll() {
        return feedbackService.findAll();
    }

    @PreAuthorize("hasAnyAuthority('USER','PERFORMER','ADMIN')")
    @GetMapping("/{id}")
    public FeedbackResponseDto findById(@PathVariable Long id) {
        return feedbackService.findById(id);
    }

    @PreAuthorize("hasAnyAuthority('USER','PERFORMER','ADMIN')")
    @GetMapping("/task/{taskId}")
    public List<FeedbackResponseDto> findByTaskID(@PathVariable Long taskId) {
        return feedbackService.findByTaskId(taskId);
    }

    // Достать все фитбеки полученные конкретным юзером
    @PreAuthorize("hasAnyAuthority('USER','PERFORMER','ADMIN')")
    @GetMapping("/user/{userId}")
    public List<FeedbackResponseDto> findReceivedForUserId(@PathVariable Long userId) {
        return feedbackService.findAllForUserId(userId);
    }
    //TODO tests method
    // Достать все фитбеки полученные конкретным юзером
    @PreAuthorize("hasAnyAuthority('USER','PERFORMER','ADMIN')")
    @GetMapping("/current")
    public List<FeedbackResponseDto> findReceivedForCurrentUser() {
        return feedbackService.findAllForCurrentUser();
    }

    // Достать все фитбеки полученные конкретным перформером
    @PreAuthorize("hasAnyAuthority('USER','PERFORMER','ADMIN')")
    @GetMapping("/performer/{performerId}")
    public List<FeedbackResponseDto> findReceivedForPerformerId(@PathVariable Long performerId) {
        return feedbackService.findAllForPerformerId(performerId);
    }

}
