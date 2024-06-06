package codezilla.handynestproject.mapper;

import codezilla.handynestproject.dto.Chat.ChatRequestDto;
import codezilla.handynestproject.dto.Chat.ChatResponseDto;
import codezilla.handynestproject.model.entity.Chat;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring",uses = UserMapper.class)
public interface ChatMapper {


    ChatResponseDto toChatResponseDto(Chat chat);

    Chat toChatFromDto(ChatResponseDto chatResponseDto);

    Chat toChat(ChatRequestDto chatRequestDto);

    List<Chat> toChatList(List<ChatRequestDto> chatRequestDtoList);

    List<ChatResponseDto> toChatResponseDtoList(List<Chat> chats);


}
