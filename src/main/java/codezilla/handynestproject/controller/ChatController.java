package codezilla.handynestproject.controller;

import codezilla.handynestproject.dto.message.MessageRequestDto;
import codezilla.handynestproject.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final UserService userService;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(MessageRequestDto message) {
        simpMessagingTemplate.convertAndSend("/topic/" + message.getChatId(), message);
    }

    @MessageMapping("/chat.addUser")
    public void addUser(MessageRequestDto message, SimpMessageHeaderAccessor headerAccessor) {
        String username = userService.findById(message.getSenderId()).getFirstName();
        headerAccessor.getSessionAttributes().put("username", username);
        simpMessagingTemplate.convertAndSend("/topic/" + message.getChatId(), message);
    }


}
