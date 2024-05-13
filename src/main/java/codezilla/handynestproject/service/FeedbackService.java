package codezilla.handynestproject.service;

import codezilla.handynestproject.dto.feedback.FeedbackResponseDto;

import java.util.List;


public interface FeedbackService {

    List<FeedbackResponseDto> getAllFeedback();

    FeedbackResponseDto getFeedbackById(Long id);
}