package codezilla.handynestproject.repository;

import codezilla.handynestproject.model.entity.Task;
import codezilla.handynestproject.model.enums.TaskStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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
    List<Task> findTasksByPerformerId(Long performerId);

    @Query("SELECT t FROM Task t WHERE t.id = :id AND t.taskStatus != 'OPEN' AND (t.performer.id = :userId OR t.user.id = :userId)")
    Optional<Task> findTaskByIdAndStatusIsNotOPENAndPerformerOrUser(Long id, Long userId);


}
