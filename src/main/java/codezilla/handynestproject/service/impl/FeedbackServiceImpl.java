package codezilla.handynestproject.service.impl;

import codezilla.handynestproject.dto.feedback.FeedbackResponseDto;
import codezilla.handynestproject.exception.FeedbackNotFoundException;
import codezilla.handynestproject.mapper.FeedbackMapper;
import codezilla.handynestproject.model.entity.Feedback;
import codezilla.handynestproject.repository.FeedbackRepository;
import codezilla.handynestproject.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final FeedbackMapper feedbackMapper;

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
}
