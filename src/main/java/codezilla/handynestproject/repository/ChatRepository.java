package codezilla.handynestproject.repository;


import codezilla.handynestproject.model.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    Optional<Chat> findByChatId(String chatId);
}
