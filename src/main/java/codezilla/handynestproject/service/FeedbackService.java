package codezilla.handynestproject.service;

import codezilla.handynestproject.dto.feedback.FeedbackCreateRequestDto;
import codezilla.handynestproject.dto.feedback.FeedbackResponseDto;

import java.util.List;


public interface FeedbackService {

    List<FeedbackResponseDto> findAll();

    FeedbackResponseDto findById(Long id);

    List<FeedbackResponseDto> findByTaskId(Long taskId);

    List<FeedbackResponseDto> findBySenderId(Long senderId);

    FeedbackResponseDto add(FeedbackCreateRequestDto requestDto);
}
