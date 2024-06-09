package codezilla.handynestproject.service;

import codezilla.handynestproject.model.entity.Chat;

public interface ChatService {
    Chat getOrCreateChat(String username1, String username2);
}
