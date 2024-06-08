package codezilla.handynestproject.mapper;


import codezilla.handynestproject.dto.feedback.FeedbackCreateRequestDto;
import codezilla.handynestproject.dto.feedback.FeedbackResponseDto;
import codezilla.handynestproject.model.entity.Feedback;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface FeedbackMapper {

    @Mapping(target = "taskId", source = "task.id")
    FeedbackResponseDto feedbackToDto(Feedback feedback);

    List<FeedbackResponseDto> feedbackToListDto(List<Feedback> feedbacks);

    Feedback createDtoToFeedback(FeedbackCreateRequestDto requestDto);
}
