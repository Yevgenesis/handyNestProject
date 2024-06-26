package codezilla.handynestproject.repository;

import codezilla.handynestproject.model.entity.Category;
import codezilla.handynestproject.model.entity.Task;
import codezilla.handynestproject.model.enums.TaskStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    @EntityGraph(value = "Task.withAddressAndCategoryAndUserAndPerformer",
            type = EntityGraph.EntityGraphType.LOAD)
    List<Task> findAll();

    @EntityGraph(value = "Task.withAddressAndCategoryAndUserAndPerformer",
            type = EntityGraph.EntityGraphType.LOAD)
    Optional<Task> findById(Long id);

    Task save(Task task);

    List<Task> findByUserId(Long userId);

    List<Task> findTaskByTaskStatus(TaskStatus taskStatus);

    List<Task> findAllByPerformerId(Long performerId);

    @Query("SELECT t FROM Task t WHERE t.id = :id AND t.taskStatus != 'OPEN' AND (t.performer.id = :userId OR t.user.id = :userId)")
    Optional<Task> findTaskByIdAndStatusIsNotOPENAndPerformerOrUser(Long id, Long userId);

    // Get all completed tasks for the performer to which he needs to send feedbacks
    @Query("SELECT t FROM Task t " +
            "LEFT JOIN Feedback f ON t.id = f.task.id AND f.sender.id = :performerId " +
            "WHERE t.taskStatus != 'OPEN' AND t.performer.id = :performerId " +
            "AND f.id IS NULL")
    List<Task> findUnrefereedByPerformerId(Long performerId);

    // Get all completed tasks for the user to which he needs to send feedbacks
    @Query("SELECT t FROM Task t " +
            "LEFT JOIN Feedback f ON t.id = f.task.id AND f.sender.id = :userId " +
            "WHERE t.taskStatus != 'OPEN' AND t.user.id = :userId " +
            "AND f.id IS NULL")
    List<Task> findUnrefereedByUserId(Long userId);

    // Get all OPEN tasks that match the categories from the provided collection
    List<Task> findAllByTaskStatusAndCategoryIn(TaskStatus taskStatus, Collection<Category> category);
}
