package codezilla.handynestproject.service.impl;

import codezilla.handynestproject.dto.feedback.FeedbackCreateRequestDto;
import codezilla.handynestproject.dto.feedback.FeedbackResponseDto;
import codezilla.handynestproject.exception.FeedbackErrorException;
import codezilla.handynestproject.exception.FeedbackNotFoundException;
import codezilla.handynestproject.mapper.FeedbackMapper;
import codezilla.handynestproject.model.entity.Feedback;
import codezilla.handynestproject.model.entity.Task;
import codezilla.handynestproject.model.entity.User;
import codezilla.handynestproject.repository.FeedbackRepository;
import codezilla.handynestproject.service.FeedbackService;
import codezilla.handynestproject.service.PerformerService;
import codezilla.handynestproject.service.TaskService;
import codezilla.handynestproject.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final FeedbackMapper feedbackMapper;
    private final TaskService taskService;
    private final PerformerService performerService;
    private final UserService userService;

    public List<FeedbackResponseDto> getAllFeedback() {
        List<Feedback> feedbackRepositoryList = feedbackRepository.findAll();
        List<FeedbackResponseDto> feedbackResponseDtoList = feedbackMapper.feedbackToListDto(feedbackRepositoryList);
        return feedbackResponseDtoList;
    }

    @Override
    public FeedbackResponseDto getFeedbackById(Long id) {
        Optional<Feedback> feedbackResponse = feedbackRepository.findById(id);
        FeedbackResponseDto feedbackResponseDto = feedbackMapper.feedbackToDto(feedbackResponse.orElseThrow(FeedbackNotFoundException::new));
        return feedbackResponseDto;
    }

    @Override
    public List<FeedbackResponseDto> getFeedbackByTaskId(Long taskId) {
        List<Feedback> feedbacks = feedbackRepository.findFeedbackByTaskId(taskId);
        List<FeedbackResponseDto> feedbacksDtos = feedbackMapper.feedbackToListDto(feedbacks);
        return feedbacksDtos;
    }

    @Override
    public List<FeedbackResponseDto> getFeedbackBySenderId(Long senderId) {
        List<Feedback> feedbacks = feedbackRepository.findFeedbackBySenderId(senderId);
        List<FeedbackResponseDto> feedbacksDtos = feedbackMapper.feedbackToListDto(feedbacks);
        return feedbacksDtos;
    }


    // ToDo оптимизировать запросы
    @Override
    @Transactional
    public FeedbackResponseDto addFeedback(FeedbackCreateRequestDto dto) {
        // ToDo сделать подробное исключение
        Task task = taskService.getTaskEntityByIdAndParticipantsId(dto.getTaskId(), dto.getSenderId());
        List<Feedback> feedbacks = feedbackRepository.findFeedbackByTaskId(dto.getTaskId());

        if (task.getPerformer() == null)
            throw new FeedbackErrorException("You can't send feedback for task with status " + task.getTaskStatus());

        // Проверка запрещает, если для этого таска уже оставлены 2 фидбека или участник хочет оставить ещё одни отзыв
        if (feedbacks.size() == 2 || feedbacks.get(0).getSender().getId().equals(dto.getSenderId())) {
            throw new FeedbackErrorException("You can't send feedback more than once for this task");
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

        // после добавления feedback обновить рейтинги и записать юзеру и перформеру
        userService.updateRating(task.getUser());
        performerService.updateRating(task.getPerformer());

        return feedbackMapper.feedbackToDto(savedFeedback);
    }

}
