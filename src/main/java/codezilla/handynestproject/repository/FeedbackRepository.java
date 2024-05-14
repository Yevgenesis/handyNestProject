package codezilla.handynestproject.repository;

import codezilla.handynestproject.model.entity.Feedback;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    @EntityGraph(value = "FeedbackWithUserAndTask", type = EntityGraph.EntityGraphType.LOAD)
    List<Feedback> findFeedbackByTaskId(Long taskId);

    @EntityGraph(value = "FeedbackWithUserAndTask", type = EntityGraph.EntityGraphType.LOAD)
    List<Feedback> findAll();

    @EntityGraph(value = "FeedbackWithUserAndTask", type = EntityGraph.EntityGraphType.LOAD)
    Optional<Feedback> findById(Long id);

    @EntityGraph(value = "FeedbackWithUserAndTask", type = EntityGraph.EntityGraphType.LOAD)
    List<Feedback> findFeedbackBySenderId(Long senderId);
}
