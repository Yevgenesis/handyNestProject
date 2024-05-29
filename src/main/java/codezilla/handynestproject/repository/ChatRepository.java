package codezilla.handynestproject.repository;


import codezilla.handynestproject.model.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {

    Optional<Chat> findById(Long id);

//    @Query("SELECT c FROM Chat c JOIN c.task t WHERE t.id = :taskId")
//    Optional<Chat> findByTaskId(Long taskId);
//
//    @Query("SELECT c FROM Chat c JOIN c.senders s WHERE s.id = :userId OR :userId MEMBER OF c.receivers")
//    Optional<Chat> findByUserId(Long chatId);

//    @Query("SELECT c FROM Chat c JOIN c.task t JOIN c.senders s WHERE t.id = :taskId " +
//            "AND (s.id = :userId OR :userId MEMBER OF c.receivers)")
//    Optional<Chat> findByTaskIdAndUserId(Long taskId, Long userId);




}
