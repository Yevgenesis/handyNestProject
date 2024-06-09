package codezilla.handynestproject.service.impl;

import codezilla.handynestproject.dto.message.MessageRequestDto;
import codezilla.handynestproject.dto.message.MessageResponseDto;
import codezilla.handynestproject.exception.MessageNotFoundException;
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

/**
 * Implementation of the MessageService interface.
 */

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {


    private final MessageRepository messageRepository;

    private final ChatService chatService;
    private final UserService userService;

    private final MessageMapper messageMapper;
    private final ChatMapper chatMapper;


    /**
     * Sends a new message.
     *
     * @param requestDto The message request DTO
     * @return The created message
     */
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
//          chat chat = chatMapper.toChatFromDto(chatResponseDto);
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

    /**
     * Marks a message as read.
     *
     * @param id The ID of the message to mark as read
     * @throws MessageNotFoundException when message not found
     */
    @Transactional
    @Override
    public void markAsRead(Long id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new MessageNotFoundException("Message not found"));
        message.setRead(true);
        messageRepository.save(message);
    }

    /**
     * Retrieves unread messages for a given user.
     *
     * @param userId The ID of the user
     * @return A list of unread message DTOs
     */
    @Transactional(readOnly = true)
    @Override
    public List<MessageResponseDto> getUnreadMessages(Long userId) {
        List<Message> unreadMessages = messageRepository.findBySenderIdAndIsReadFalse(userId);
        return unreadMessages.stream()
                .map(messageMapper::toMessageResponseDto)
                .collect(Collectors.toList());
    }


}
