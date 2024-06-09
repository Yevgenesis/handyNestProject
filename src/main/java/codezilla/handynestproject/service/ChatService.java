package codezilla.handynestproject.service;

import codezilla.handynestproject.dto.chat.ChatRequestDto;
import codezilla.handynestproject.dto.chat.ChatResponseDto;

import java.util.List;

public interface ChatService {

    ChatResponseDto create(ChatRequestDto chatRequestDto);

    ChatResponseDto findById(Long id);

    List<ChatResponseDto> findAll();

    boolean existsById(Long id);

    void closedById(Long id);
}
