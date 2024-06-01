package codezilla.handynestproject.service.impl;

import codezilla.handynestproject.dto.Chat.ChatRequestDto;
import codezilla.handynestproject.dto.Chat.ChatResponseDto;
import codezilla.handynestproject.dto.message.MessageRequestDto;
import codezilla.handynestproject.dto.message.MessageResponseDto;
import codezilla.handynestproject.mapper.ChatMapper;
import codezilla.handynestproject.mapper.MessageMapper;
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

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {


    private final MessageRepository messageRepository;

    private final ChatService chatService;
    private final UserService userService;

    private final MessageMapper messageMapper;
    private final ChatMapper chatMapper;


    @Transactional
    @Override
    public Message send(MessageRequestDto requestDto) {
        Chat chat = chatMapper.toChatFromDto(chatService.findById(requestDto.getChatId()));
//        if(requestDto.getChatId() == null){
//            ChatRequestDto chatRequestDto = ChatRequestDto.builder()
//                    .userId(requestDto.getSenderId())
//                    .performerId(requestDto.getSenderId())
//                    .build();
//          ChatResponseDto chatResponseDto = chatService.create(chatRequestDto);
//          Chat chat = chatMapper.toChatFromDto(chatResponseDto);
//
//        }
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

    @Transactional(readOnly = true)
    @Override
    public List<MessageResponseDto> getUnreadMessages(Long userId) {
        List<Message> unreadMessages = messageRepository.findBySenderIdAndIsReadFalse(userId);
        return unreadMessages.stream()
                .map(messageMapper::toMessageResponseDto)
                .collect(Collectors.toList());
    }


}
