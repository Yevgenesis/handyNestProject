package codezilla.handynestproject.service;

import codezilla.handynestproject.dto.message.MessageRequestDto;
import codezilla.handynestproject.model.entity.Message;

import java.util.List;

public interface MessageService {

    Message send(MessageRequestDto messageRequestDto);
    List<Message> findByUserId(Long id);
    void markAsRead(Long id);



}
