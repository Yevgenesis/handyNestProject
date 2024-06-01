package codezilla.handynestproject.service;

import codezilla.handynestproject.dto.message.MessageRequestDto;
import codezilla.handynestproject.dto.message.MessageResponseDto;
import codezilla.handynestproject.model.entity.Message;

import java.util.List;

public interface MessageService {

    Message send(MessageRequestDto messageRequestDto);

    void markAsRead(Long id);
    List<MessageResponseDto> getUnreadMessages(Long userId);


}
