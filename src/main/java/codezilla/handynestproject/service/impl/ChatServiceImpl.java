package codezilla.handynestproject.service.impl;

import codezilla.handynestproject.dto.Chat.ChatRequestDto;
import codezilla.handynestproject.dto.Chat.ChatResponseDto;
import codezilla.handynestproject.exception.ChatNotFoundException;
import codezilla.handynestproject.exception.TaskWrongStatusException;
import codezilla.handynestproject.mapper.ChatMapper;
import codezilla.handynestproject.mapper.TaskMapper;
import codezilla.handynestproject.model.entity.Chat;
import codezilla.handynestproject.model.entity.Performer;
import codezilla.handynestproject.model.entity.Task;
import codezilla.handynestproject.model.entity.User;
import codezilla.handynestproject.model.enums.TaskStatus;
import codezilla.handynestproject.repository.ChatRepository;
import codezilla.handynestproject.service.ChatService;
import codezilla.handynestproject.service.PerformerService;
import codezilla.handynestproject.service.TaskService;
import codezilla.handynestproject.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing chats.
 */

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatRepository chatRepository;
    private final TaskService taskService;
    private final UserService userService;
    private final PerformerService performerService;


    private final TaskMapper taskMapper;
    private final ChatMapper chatMapper;

    /**
     * Creates a new chat.
     *
     * @param chatRequestDto The chat request DTO.
     * @return The created chat DTO.
     */
    @Override
    @Transactional
    public ChatResponseDto create(ChatRequestDto chatRequestDto) {
        Task task = taskMapper.toTask(taskService.findById(chatRequestDto.getTaskId()));
        if (task.getTaskStatus().equals(TaskStatus.CANCELED)
                || task.getTaskStatus().equals(TaskStatus.COMPLETED)) {
            throw new TaskWrongStatusException("Task status can't be " + task.getTaskStatus());
        }
        User user = userService.findByIdReturnUser(chatRequestDto.getUserId());
        Performer performer = performerService.findByIdReturnPerformer(chatRequestDto.getPerformerId());
        Chat chat = Chat.builder()
                .task(task)
                .user(user)
                .performer(performer)
                .build();

        return chatMapper.toChatResponseDto(chatRepository.save(chat));
    }

    /**
     * Finds a chat by its ID.
     *
     * @param id The ID of the chat to find.
     * @return The found chat DTO.
     */
    @Override
    public ChatResponseDto findById(Long id) {
        return chatMapper.toChatResponseDto(chatRepository.findById(id)
                .orElseThrow(() -> new ChatNotFoundException("Chat with id " + id + " not found")));
    }

    /**
     * Finds all chats.
     *
     * @return A list of chat DTOs.
     */
    @Override
    public List<ChatResponseDto> findAll() {
        return chatMapper.toChatResponseDtoList(chatRepository.findAll());
    }

    /**
     * Checks if a chat exists by its ID.
     *
     * @param id The ID of the chat to check.
     * @return True if the chat exists, false otherwise.
     */
    @Override
    public boolean existsById(Long id) {
        return chatRepository.existsById(id);
    }

    /**
     * Closes a chat by its ID.
     *
     * @param id The ID of the chat to close.
     */
    @Override
    public void closedById(Long id) {
        Chat chat = chatRepository.findById(id)
                .orElseThrow(() -> new ChatNotFoundException("Chat with id " + id + " not found"));
        chat.setDeleted(true);
    }
}
