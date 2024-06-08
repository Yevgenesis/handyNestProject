package codezilla.handynestproject.controller;

import codezilla.handynestproject.dto.message.MessageRequestDto;
import codezilla.handynestproject.dto.message.MessageResponseDto;
import codezilla.handynestproject.mapper.MessageMapper;
import codezilla.handynestproject.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.sisu.Hidden;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
@Hidden
public class WebSocketController {

    private final MessageService messageService;
    private final MessageMapper messageMapper;

    /**
     * Sends a message via WebSocket.
     *
     * @param messageRequestDto The message request DTO
     * @return The response DTO after sending the message
     */
    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    public MessageResponseDto sendMessage(MessageRequestDto messageRequestDto) {
        return messageMapper.toMessageResponseDto(messageService.send(messageRequestDto));
    }
}

