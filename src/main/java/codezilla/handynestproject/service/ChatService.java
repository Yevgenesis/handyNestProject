package codezilla.handynestproject.service;

import codezilla.handynestproject.dto.Chat.ChatRequestDto;
import codezilla.handynestproject.dto.Chat.ChatResponseDto;

import java.util.List;

public interface ChatService {

    ChatResponseDto create(ChatRequestDto chatRequestDto);

    ChatResponseDto findById(Long id);

    List<ChatResponseDto> findAll();

    boolean existsById(Long id);

    void closedById(Long id);
}
