package codezilla.handynestproject.repository;

import codezilla.handynestproject.model.entity.Task;
import codezilla.handynestproject.model.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findAll();
    Optional<Task> findById(Long id);
    Optional<Task> findByUserId(Long userId);
    List<Task> findTaskByTaskStatus(TaskStatus taskStatus);
    List<Task> findTasksByPerformerId(Long performerId);



}
