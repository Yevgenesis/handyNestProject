package codezilla.handynestproject.controller;

import codezilla.handynestproject.dto.message.MessageRequestDto;
import codezilla.handynestproject.model.entity.Message;
import codezilla.handynestproject.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    public Message send(@RequestBody MessageRequestDto messageRequestDto) {
        return messageService.send(messageRequestDto);
    }

    @GetMapping("/{id}")
    public List<Message> findAllByUser(@PathVariable Long id) {
        return messageService.findByUserId(id);
    }

    @PutMapping("/{messageId}/read")
    public void markRead(@PathVariable Long messageId) {
        messageService.markAsRead(messageId);
    }
}
