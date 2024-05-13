package codezilla.handynestproject.mapper;


import codezilla.handynestproject.dto.feedback.FeedbackResponseDto;
import codezilla.handynestproject.model.entity.Feedback;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FeedbackMapper {

    @Mapping(source = "sender.id", target = "senderId")
    FeedbackResponseDto feedbackToDto(Feedback feedback);

    List<FeedbackResponseDto> feedbackToListDto(List<Feedback> feedbacks);


}
