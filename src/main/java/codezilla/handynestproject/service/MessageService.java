package codezilla.handynestproject.service;

import codezilla.handynestproject.model.entity.Message;

import java.util.List;

public interface MessageService {

    Message send(Long senderId, Long receiverId, String text);
    List<Message> findBySenderIdOrReceiverId(Long id);
    void markAsRead(Long id);

}
