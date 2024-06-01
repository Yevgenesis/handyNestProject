package codezilla.handynestproject.controller;

import codezilla.handynestproject.dto.message.MessageRequestDto;
import codezilla.handynestproject.dto.message.MessageResponseDto;
import codezilla.handynestproject.mapper.MessageMapper;
import codezilla.handynestproject.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

    private final MessageService messageService;
    private final MessageMapper messageMapper;


    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    public MessageResponseDto sendMessage(MessageRequestDto messageRequestDto) {
        return messageMapper.toMessageResponseDto(messageService.send(messageRequestDto));
    }
}

