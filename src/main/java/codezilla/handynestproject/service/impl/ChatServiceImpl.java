package codezilla.handynestproject.service.impl;

import codezilla.handynestproject.dto.Chat.ChatRequestDto;
import codezilla.handynestproject.dto.Chat.ChatResponseDto;
import codezilla.handynestproject.exception.ChatNotFoundException;
import codezilla.handynestproject.mapper.ChatMapper;
import codezilla.handynestproject.model.entity.Chat;
import codezilla.handynestproject.repository.ChatRepository;
import codezilla.handynestproject.service.ChatService;
import codezilla.handynestproject.service.MessageService;
import codezilla.handynestproject.service.PerformerService;
import codezilla.handynestproject.service.TaskService;
import codezilla.handynestproject.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatRepository chatRepository;
    private final TaskService taskService;
    private final UserService userService;
    private final PerformerService performerService;
    private final MessageService messageService;
    private final ChatMapper chatMapper;


    @Override
    @Transactional
    public ChatResponseDto create(ChatRequestDto chatRequestDto) {
        taskService.existsById(chatRequestDto.getTaskId());
        taskService.canceledOrCompleted(chatRequestDto.getTaskId());
        userService.existsById(chatRequestDto.getSenderId());
        userService.existsById(chatRequestDto.getReceiverId());
        return chatMapper.toChatResponseDto(chatRepository.save(chatMapper.toChat(chatRequestDto)));
    }

    @Override
    public ChatResponseDto findById(Long id) {
        return chatMapper.toChatResponseDto(chatRepository.findById(id)
                .orElseThrow(()->new ChatNotFoundException("Chat with id " + id + " not found")));
    }

    @Override
    public List<ChatResponseDto> findAll() {
        return chatMapper.toChatResponseDtoList(chatRepository.findAll());
    }

    @Override
    public boolean existsById(Long id) {
        return chatRepository.existsById(id);
    }

    @Override
    public void closedById(Long id) {
        Chat chat = chatRepository.findById(id)
                .orElseThrow(()->new ChatNotFoundException("Chat with id " + id + " not found"));
        chat.setDeleted(true);
    }
}
