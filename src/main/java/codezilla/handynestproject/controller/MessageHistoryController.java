package codezilla.handynestproject.controller;

import codezilla.handynestproject.model.entity.Message;
import codezilla.handynestproject.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MessageHistoryController {

    @Autowired
    private MessageService messageService;

    @GetMapping("/history")
    public List<Message> getChatHistory(@RequestParam String user1, @RequestParam String user2) {
        return messageService.getChatHistory(user1, user2);
    }
}