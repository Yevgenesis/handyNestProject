package codezilla.handynestproject.service.impl;

import codezilla.handynestproject.mapper.UserMapper;
import codezilla.handynestproject.model.entity.Message;
import codezilla.handynestproject.model.entity.Notification;
import codezilla.handynestproject.repository.MessageRepository;
import codezilla.handynestproject.service.MessageService;
import codezilla.handynestproject.service.NotificationService;
import codezilla.handynestproject.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {


    private final MessageRepository messageRepository;
    private final NotificationService notificationService;
    private final UserService userService;
    private final UserMapper userMapper;

    @Override
    public Message send(Long senderId, Long receiverId, String text) {
        Message message = Message.builder()
                .sender(userMapper.toUser(userService.findById(senderId)))
                .receiver(userMapper.toUser(userService.findById(receiverId)))
                .text(text)
                .time(Timestamp.valueOf(LocalDateTime.now()))
                .read(false)
                .build();
        messageRepository.save(message);

        Notification notification = Notification.builder()
                .user(userMapper.toUser(userService.findById(receiverId)))
                .content("You have a new message")
                .isRead(false)
                .build();
        notificationService.save(notification);

        return message ;
    }

    @Override
    public List<Message> findBySenderIdOrReceiverId(Long id) {
        return messageRepository.findBySenderIdOrReceiverId(id);
    }

    @Override
    public void markAsRead(Long id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        message.setRead(true);
        messageRepository.save(message);
    }
}
