package codezilla.handynestproject.service.impl;

import codezilla.handynestproject.dto.message.MessageRequestDto;
import codezilla.handynestproject.mapper.ChatMapper;
import codezilla.handynestproject.model.entity.Chat;
import codezilla.handynestproject.model.entity.Message;
import codezilla.handynestproject.model.entity.User;
import codezilla.handynestproject.repository.MessageRepository;
import codezilla.handynestproject.service.ChatService;
import codezilla.handynestproject.service.MessageService;
import codezilla.handynestproject.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {


    private final MessageRepository messageRepository;

    private final ChatService chatService;
    private final UserService userService;

    private final ChatMapper chatMapper;


    @Transactional
    @Override
    public Message send(MessageRequestDto requestDto) {
        Chat chat = chatMapper.toChatFromDto(chatService.findById(requestDto.getChatId()));
        User sender = userService.findByIdReturnUser(requestDto.getSenderId());

        Message message = Message.builder()
                .chat(chat)
                .sender(sender)
                .text(requestDto.getText())
                .isRead(false)
                .build();

        Message savedMessage = messageRepository.save(message);


        return savedMessage;
    }

    @Transactional
    @Override
    public void markAsRead(Long id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        message.setRead(true);
        messageRepository.save(message);
    }


}
