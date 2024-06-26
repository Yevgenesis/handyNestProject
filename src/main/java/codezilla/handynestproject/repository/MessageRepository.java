package codezilla.handynestproject.repository;

import codezilla.handynestproject.model.entity.Message;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @EntityGraph(value = "MessageWithSenderAndChat", type = EntityGraph.EntityGraphType.LOAD)
    Optional<Message> findById(Long id);

    @Query("SELECT m FROM Message m WHERE m.chat.id = :chatId")
    List<Message> findByChatId(Long chatId);

    List<Message> findBySenderIdAndIsReadFalse(Long userId);
}
