package codezilla.handynestproject.service;

import codezilla.handynestproject.dto.feedback.FeedbackCreateRequestDto;
import codezilla.handynestproject.dto.feedback.FeedbackResponseDto;

import java.util.List;


public interface FeedbackService {

    FeedbackResponseDto add(FeedbackCreateRequestDto requestDto);

    List<FeedbackResponseDto> findAll();

    FeedbackResponseDto findById(Long id);

    List<FeedbackResponseDto> findAllByTaskId(Long taskId);

    List<FeedbackResponseDto> findAllBySenderId(Long senderId);

    List<FeedbackResponseDto> findAllReceivedByPerformerId(Long performerId);

    // Достать все фитбеки полученные конкретным юзером
    List<FeedbackResponseDto> findAllReceivedByUserId(Long userId);

    List<FeedbackResponseDto> findAllForCurrentUser();
}
