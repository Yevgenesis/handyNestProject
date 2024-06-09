package codezilla.handynestproject.controller;


import codezilla.handynestproject.dto.message.MessageDto;
import codezilla.handynestproject.model.entity.Message;
import codezilla.handynestproject.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {

    @Autowired
    private MessageService messageService;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public MessageDto sendMessage(@Payload MessageDto messageDto) {
        Message savedMessage = messageService.saveMessage(messageDto.getSender(), messageDto.getReceiver(), messageDto.getContent());
        messagingTemplate.convertAndSendToUser(messageDto.getReceiver(), "/queue/messages", messageDto);
        return messageDto;
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public MessageDto addUser(@Payload MessageDto messageDto) {
        return messageDto;
    }
}
