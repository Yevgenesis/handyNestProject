package codezilla.handynestproject.service.impl;

import codezilla.handynestproject.dto.message.MessageRequestDto;
import codezilla.handynestproject.mapper.ChatMapper;
import codezilla.handynestproject.mapper.UserMapper;
import codezilla.handynestproject.model.entity.Chat;
import codezilla.handynestproject.model.entity.Message;
import codezilla.handynestproject.repository.MessageRepository;
import codezilla.handynestproject.service.ChatService;
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
    private final ChatService chatService;
    private final UserService userService;
    private final UserMapper userMapper;
    private final ChatMapper chatMapper;

    @Override
    public Message send(MessageRequestDto requestDto) {
        chatService.existsById(requestDto.getChatId());
        userService.existsById(requestDto.getSenderId());
        userService.existsById(requestDto.getReceiverId());

        Message message = Message.builder()
                .chat(chatMapper.toChatFromDto(chatService.findById(requestDto.getChatId())))
                .sender(userService.findByIdReturnUser(requestDto.getSenderId()))
                .receiver(userService.findByIdReturnUser(requestDto.getReceiverId()))
                .text(requestDto.getText())
                .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                .updatedOn(Timestamp.valueOf(LocalDateTime.now()))
                .read(false)
                .build();

        return messageRepository.save(message);
    }

    @Override
    public List<Message> findByUserId(Long id) {
        userService.existsById(id);
        return messageRepository.findByUserId(id);
    }

    @Override
    public void markAsRead(Long id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        message.setRead(true);
        messageRepository.save(message);
    }
}
