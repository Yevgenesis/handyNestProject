package codezilla.handynestproject.mapper;

import codezilla.handynestproject.dto.message.MessageRequestDto;
import codezilla.handynestproject.dto.message.MessageResponseDto;
import codezilla.handynestproject.model.entity.Message;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class, ChatMapper.class} )
public interface MessageMapper {


    MessageResponseDto toMessageResponseDto(Message message);

    List<MessageResponseDto> toMessageResponseDtoList(List<Message> messages);

    Message toMessage(MessageRequestDto messageRequestDto);
}
