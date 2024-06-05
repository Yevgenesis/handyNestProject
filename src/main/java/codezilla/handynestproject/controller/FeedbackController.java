package codezilla.handynestproject.controller;

import codezilla.handynestproject.dto.feedback.FeedbackCreateRequestDto;
import codezilla.handynestproject.dto.feedback.FeedbackResponseDto;
import codezilla.handynestproject.service.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/feedbacks")
@RequiredArgsConstructor
@Tag(name = "Feedback Controller", description = "Operations related to feedbacks")
public class FeedbackController {

    private final FeedbackService feedbackService;


    /**
     * Adds new feedback.
     *
     * @param requestDto the feedback creation request DTO
     * @return the created feedback
     */
    @Operation(summary = "Add feedback", description = "Return added feedback",
            security = @SecurityRequirement(name = "feedbacks"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully added feedback"),
            @ApiResponse(responseCode = "404", description = "Feedback not created")
    })
    @PreAuthorize("hasAnyAuthority('USER','PERFORMER')")
    @PostMapping
    public FeedbackResponseDto add(@RequestBody @Valid FeedbackCreateRequestDto requestDto) {
        log.info("Created new feedback: {}", requestDto);
        return feedbackService.add(requestDto);
    }


    /**
     * Retrieves all feedbacks.
     *
     * @return a list of all feedbacks
     */
    @Operation(summary = "Get all feedbacks", description = "Return all feedbacks",
            security = @SecurityRequirement(name = "feedbacks"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved feedbacks"),
            @ApiResponse(responseCode = "404", description = "Feedbacks not found")
    })
    @PreAuthorize("hasAnyAuthority('USER','PERFORMER','ADMIN')")
    @GetMapping
    public List<FeedbackResponseDto> findAll() {
        log.info("All feedbacks");
        return feedbackService.findAll();
    }

    /**
     * Retrieves feedback by ID.
     *
     * @param id the feedback ID
     * @return the feedback with the specified ID
     */
    @Operation(summary = "Get feedback by id", description = "Return feedback by id",
            security = @SecurityRequirement(name = "feedbacks"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved feedback"),
            @ApiResponse(responseCode = "404", description = "Feedback not found")
    })
    @PreAuthorize("hasAnyAuthority('USER','PERFORMER','ADMIN')")
    @GetMapping("/{id}")
    public FeedbackResponseDto findById(@PathVariable Long id) {
        log.info("Find feedback by id: {}", id);
        return feedbackService.findById(id);
    }

    /**
     * Retrieves all feedbacks for a specific task.
     *
     * @param taskId the task ID
     * @return a list of feedbacks for the specified task
     */
    @Operation(summary = "Get feedback by task id", description = "Return feedback by task id",
            security = @SecurityRequirement(name = "feedbacks"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved feedbacks"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @PreAuthorize("hasAnyAuthority('USER','PERFORMER','ADMIN')")
    @GetMapping("/task/{taskId}")
    public List<FeedbackResponseDto> findByTaskID(@PathVariable Long taskId) {
        log.info("Find feedback by task id: {}", taskId);
        return feedbackService.findAllByTaskId(taskId);


    }

    /**
     * Retrieves all feedbacks received by a specific user.
     *
     * @param userId the user ID
     * @return a list of feedbacks received by the specified user
     */
    @Operation(summary = "Get all received feedbacks by user id",
            description = "Return all received feedbacks by user id",
            security = @SecurityRequirement(name = "feedbacks"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved feedbacks"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasAnyAuthority('USER','PERFORMER','ADMIN')")
    @GetMapping("/user/{userId}")
    public List<FeedbackResponseDto> findReceivedByUserId(@PathVariable Long userId) {
        log.info("Find received feedbacks by user id: {}", userId);
        return feedbackService.findAllReceivedByUserId(userId);
    }
    //TODO tests method
    // Достать все фитбеки полученные конкретным юзером

    /**
     * Retrieves all feedbacks of the current authenticated user.
     *
     * @return a list of feedbacks of the current user
     */
    @Operation(summary = "Get all received feedbacks of an authorized user",
            description = "Return all received feedbacks of an authorized user",
            security = @SecurityRequirement(name = "feedbacks"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved feedbacks"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasAnyAuthority('USER','PERFORMER','ADMIN')")
    @GetMapping("/current")
    public List<FeedbackResponseDto> findReceivedForCurrentUser() {
        log.info("Find received feedbacks of current user");
        return feedbackService.findAllForCurrentUser();
    }

    /**
     * Retrieves all feedbacks received by a specific performer.
     *
     * @param performerId the performer ID
     * @return a list of feedbacks received by the specified performer
     */
    @Operation(summary = "Get all received feedbacks by performer id",
            description = "Return all received feedbacks by performer id",
            security = @SecurityRequirement(name = "feedbacks"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved feedbacks"),
            @ApiResponse(responseCode = "404", description = "Performer not found")
    })
    @PreAuthorize("hasAnyAuthority('USER','PERFORMER','ADMIN')")
    @GetMapping("/performer/{performerId}")
    public List<FeedbackResponseDto> findReceivedForPerformerId(@PathVariable Long performerId) {
        log.info("Find received feedbacks by performer id: {}", performerId);
        return feedbackService.findAllReceivedByPerformerId(performerId);
    }

}
