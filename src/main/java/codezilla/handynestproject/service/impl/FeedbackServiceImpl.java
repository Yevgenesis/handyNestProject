package codezilla.handynestproject.service.impl;

import codezilla.handynestproject.dto.feedback.FeedbackCreateRequestDto;
import codezilla.handynestproject.dto.feedback.FeedbackResponseDto;
import codezilla.handynestproject.exception.FeedbackErrorException;
import codezilla.handynestproject.exception.FeedbackNotFoundException;
import codezilla.handynestproject.mapper.FeedbackMapper;
import codezilla.handynestproject.model.entity.Feedback;
import codezilla.handynestproject.model.entity.Task;
import codezilla.handynestproject.model.entity.User;
import codezilla.handynestproject.model.enums.TaskStatus;
import codezilla.handynestproject.repository.FeedbackRepository;
import codezilla.handynestproject.security.UserDetailsServiceImpl;
import codezilla.handynestproject.service.FeedbackService;
import codezilla.handynestproject.service.PerformerService;
import codezilla.handynestproject.service.TaskService;
import codezilla.handynestproject.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of the FeedbackService interface.
 */

@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final FeedbackMapper feedbackMapper;
    private final TaskService taskService;
    private final PerformerService performerService;
    private final UserService userService;
    private final UserDetailsServiceImpl userDetailsService;

    /**
     * Finds all feedback.
     *
     * @return A list of feedback DTOs
     */
    public List<FeedbackResponseDto> findAll() {
        List<Feedback> feedbackRepositoryList = feedbackRepository.findAll();
        List<FeedbackResponseDto> feedbackResponseDtoList = feedbackMapper.feedbackToListDto(feedbackRepositoryList);
        return feedbackResponseDtoList;
    }

    /**
     * Finds feedback by its ID.
     *
     * @param id The ID of the feedback to find
     * @return The found feedback DTO
     */
    @Override
    public FeedbackResponseDto findById(Long id) {
        Optional<Feedback> feedbackResponse = feedbackRepository.findById(id);
        FeedbackResponseDto feedbackResponseDto = feedbackMapper.feedbackToDto(feedbackResponse.orElseThrow(FeedbackNotFoundException::new));
        return feedbackResponseDto;
    }

    /**
     * Finds all feedback associated with a given task.
     *
     * @param taskId The ID of the task
     * @return A list of feedback DTOs
     */
    @Override
    public List<FeedbackResponseDto> findAllByTaskId(Long taskId) {
        taskService.findById(taskId);
        List<Feedback> feedbacks = feedbackRepository.findByTaskId(taskId);
        List<FeedbackResponseDto> feedbacksDtos = feedbackMapper.feedbackToListDto(feedbacks);
        return feedbacksDtos;
    }

    /**
     * Finds all feedback sent by a given user.
     *
     * @param senderId The ID of the sender
     * @return A list of feedback DTOs
     */
    @Override
    public List<FeedbackResponseDto> findAllBySenderId(Long senderId) {
        List<Feedback> feedbacks = feedbackRepository.findBySenderId(senderId);
        List<FeedbackResponseDto> feedbacksDtos = feedbackMapper.feedbackToListDto(feedbacks);
        return feedbacksDtos;
    }

    /**
     * Adds a new feedback.
     *
     * @param dto The feedback creation DTO
     * @return The created feedback DTO
     * @throws FeedbackErrorException when task have status OPEN
     * @throws FeedbackErrorException when task have already 2 feedbacks
     */
    @Override
    @Transactional
    public FeedbackResponseDto add(FeedbackCreateRequestDto dto) {

        Task task = taskService.findTaskEntityByIdAndParticipantsId(dto.getTaskId(), dto.getSenderId());
        List<Feedback> feedbacks = feedbackRepository.findByTaskId(dto.getTaskId());

        if (task.getTaskStatus().equals(TaskStatus.OPEN))
            throw new FeedbackErrorException("You can't send feedback for task with OPEN status");

        // Verification is prohibited if 2 feedbacks have already been
        // left for this task or the participant wants to leave another feedback
        if (!feedbacks.isEmpty()) {
            if (feedbacks.size() == 2 || feedbacks.get(0).getSender().getId().equals(dto.getSenderId())) {
                throw new FeedbackErrorException("You can't send feedback more than once for this task");
            }
        }
        // Checking participants of the task
        Long currentUserId = userDetailsService.getCurrentUserId();
        if (!(currentUserId.equals(task.getUser().getId()) || currentUserId.equals(task.getPerformer().getId()))) {
            throw new FeedbackErrorException("You can't send feedback for this task, that doesn't belong to you");
        }

        User sender;
        if (task.getUser().getId().equals(dto.getSenderId())) {
            sender = task.getUser();
        } else {
            sender = task.getPerformer().getUser();
        }

        Feedback feedback = Feedback.builder()
                .task(task)
                .grade(dto.getGrade())
                .text(dto.getText())
                .sender(sender)
                .build();
        Feedback savedFeedback = feedbackRepository.save(feedback);

        // after adding feedback, update the ratings of user and performer
        userService.updateRating(task.getUser());
        performerService.updateRating(task.getPerformer());

        return feedbackMapper.feedbackToDto(savedFeedback);
    }

    /**
     * Finds all feedback received by a given performer.
     *
     * @param performerId The ID of the performer
     * @return A list of feedback DTOs
     */
    @Override
    public List<FeedbackResponseDto> findAllReceivedByPerformerId(Long performerId) {
        performerService.findById(performerId);
        List<Feedback> feedbacks = feedbackRepository.findReceivedByPerformerId(performerId);
        return feedbackMapper.feedbackToListDto(feedbacks);
    }

    /**
     * Finds all feedback received by a given user.
     *
     * @param userId The ID of the user
     * @return A list of feedback DTOs
     */
    @Override
    public List<FeedbackResponseDto> findAllReceivedByUserId(Long userId) {
        userService.checkExists(userId);
        List<Feedback> feedbacks = feedbackRepository.findReceivedByUserId(userId);
        return feedbackMapper.feedbackToListDto(feedbacks);
    }

    /**
     * Finds all feedback received by the current user.
     *
     * @return A list of feedback DTOs
     */
    @Override
    public List<FeedbackResponseDto> findAllForCurrentUser() {
        Long currentUserId = userService.getCurrentUserId();
        return findAllReceivedByUserId(currentUserId);
    }
}
