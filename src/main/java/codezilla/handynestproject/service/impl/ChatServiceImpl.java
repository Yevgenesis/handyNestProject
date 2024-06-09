package codezilla.handynestproject.service.impl;

import codezilla.handynestproject.model.entity.Chat;
import codezilla.handynestproject.model.entity.User;
import codezilla.handynestproject.repository.ChatRepository;
import codezilla.handynestproject.service.ChatService;
import codezilla.handynestproject.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of the ChatService interface.
 */

@Service
public class ChatServiceImpl implements ChatService {
    @Autowired
    private ChatRepository chatRepository;
    @Autowired
    private UserService userService;

    @Override
    public Chat getOrCreateChat(String username1, String username2) {
        User user1 = userService.getByEmail(username1);
        User user2 = userService.getByEmail(username2);

        String chatId = user1.getEmail().compareTo(user2.getEmail()) < 0 ?
                user1.getEmail() + "_" + user2.getEmail() :
                user2.getEmail() + "_" + user1.getEmail();

        return chatRepository.findByChatId(chatId)
                .orElseGet(() -> {
                    Chat newChat = new Chat();
                    newChat.setUser1(user1);
                    newChat.setUser2(user2);
                    newChat.setChatId(chatId);
                    return chatRepository.save(newChat);
                });
    }
}
