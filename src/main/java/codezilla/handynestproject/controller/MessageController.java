package codezilla.handynestproject.controller;

import codezilla.handynestproject.dto.message.MessageRequestDto;
import codezilla.handynestproject.dto.message.MessageResponseDto;
import codezilla.handynestproject.mapper.MessageMapper;
import codezilla.handynestproject.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final MessageMapper messageMapper;

    @PostMapping
    public MessageResponseDto send(@RequestBody MessageRequestDto message) {
        return messageMapper.toMessageResponseDto(messageService.send(message));
    }

    @PutMapping("/{messageId}/read")
    public void markRead(@PathVariable Long messageId) {
        messageService.markAsRead(messageId);
    }
}
