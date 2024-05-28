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

    @EntityGraph(value = "MessageWithSenderAndReceiverAndTask", type = EntityGraph.EntityGraphType.LOAD)
    Optional<Message> findById(Long id);

    @EntityGraph(value = "MessageWithSenderAndReceiverAndTask", type = EntityGraph.EntityGraphType.LOAD)
    List<Message> findByTaskId(Long TaskId);

    @Query("SELECT m FROM Message m " +
            "JOIN Task t ON m.task.id = t.id " +
            "WHERE m.sender.id = :userId AND m.sender.id != :userId " +
            "OR m.receiver.id = :userId AND m.receiver.id != :userId")
    List<Message> findBySenderOrReceiverId(Long userId);



}
