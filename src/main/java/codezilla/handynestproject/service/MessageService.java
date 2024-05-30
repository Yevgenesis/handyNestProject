package codezilla.handynestproject.service;

import codezilla.handynestproject.dto.message.MessageRequestDto;
import codezilla.handynestproject.model.entity.Message;

public interface MessageService {

    Message send(MessageRequestDto messageRequestDto);

    void markAsRead(Long id);


}
