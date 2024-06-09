package codezilla.handynestproject.service;

import codezilla.handynestproject.model.entity.Message;

import java.util.List;

public interface MessageService {
    List<Message> getChatHistory(String username1, String username2);

    Message saveMessage(String senderUsername, String receiverUsername, String content);
}
