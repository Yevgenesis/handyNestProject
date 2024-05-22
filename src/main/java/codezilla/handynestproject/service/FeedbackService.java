package codezilla.handynestproject.service;

import codezilla.handynestproject.dto.feedback.FeedbackCreateRequestDto;
import codezilla.handynestproject.dto.feedback.FeedbackResponseDto;

import java.util.List;


public interface FeedbackService {

    FeedbackResponseDto add(FeedbackCreateRequestDto requestDto);

    List<FeedbackResponseDto> findAll();

    FeedbackResponseDto findById(Long id);

    List<FeedbackResponseDto> findByTaskId(Long taskId);

    List<FeedbackResponseDto> findBySenderId(Long senderId);

    List<FeedbackResponseDto> findAllForPerformerId(Long performerId);

    List<FeedbackResponseDto> findAllForUserId(Long userId);
}
