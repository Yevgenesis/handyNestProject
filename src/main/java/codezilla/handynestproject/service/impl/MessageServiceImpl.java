package codezilla.handynestproject.service.impl;

import codezilla.handynestproject.model.entity.Chat;
import codezilla.handynestproject.model.entity.Message;
import codezilla.handynestproject.model.entity.User;
import codezilla.handynestproject.repository.MessageRepository;
import codezilla.handynestproject.service.ChatService;
import codezilla.handynestproject.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation of the MessageService interface.
 */

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;

    private final ChatService chatService;

    @Override
    public List<Message> getChatHistory(String username1, String username2) {
        Chat chat = chatService.getOrCreateChat(username1, username2);
        return messageRepository.findByChat(chat);
    }

    @Override
    public Message saveMessage(String senderUsername, String receiverUsername, String content) {
        Chat chat = chatService.getOrCreateChat(senderUsername, receiverUsername);
        User sender = chat.getUser1().getEmail().equals(senderUsername) ? chat.getUser1() : chat.getUser2();

        Message message = new Message();
        message.setChat(chat);
        message.setSender(sender);
        message.setContent(content);
        message.setTimestamp(LocalDateTime.now());

        return messageRepository.save(message);
    }
}