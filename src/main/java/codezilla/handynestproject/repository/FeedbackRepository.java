package codezilla.handynestproject.repository;

import codezilla.handynestproject.model.entity.Feedback;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    @EntityGraph(value = "FeedbackWithUserAndTask", type = EntityGraph.EntityGraphType.LOAD)
    List<Feedback> findByTaskId(Long taskId);

    @EntityGraph(value = "FeedbackWithUserAndTask", type = EntityGraph.EntityGraphType.LOAD)
    List<Feedback> findAll();

    @EntityGraph(value = "FeedbackWithUserAndTask", type = EntityGraph.EntityGraphType.LOAD)
    Optional<Feedback> findById(Long id);

    @EntityGraph(value = "FeedbackWithUserAndTask", type = EntityGraph.EntityGraphType.LOAD)
    List<Feedback> findBySenderId(Long senderId);

    // Get all feedbacks received by a specific performer
    @Query("SELECT f FROM Feedback f " +
            "JOIN Task t ON f.task.id = t.id " +
            "WHERE t.performer.id = :performerId AND f.sender.id != :performerId")
    List<Feedback> findReceivedByPerformerId(@Param("performerId") Long performerId);

    // Get all feedbacks received by a specific user
    @Query("SELECT f FROM Feedback f " +
            "JOIN Task t ON f.task.id = t.id " +
            "WHERE t.user.id = :userId AND f.sender.id != :userId")
    List<Feedback> findReceivedByUserId(Long userId);
}
