package codezilla.handynestproject.controller;

import codezilla.handynestproject.service.messenger.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    @GetMapping("/unread/{userId}")
    public List<Message> getUnreadMessages(@PathVariable Long userId) {
        return notificationService.getUnreadMessages(userId);
    }
}
