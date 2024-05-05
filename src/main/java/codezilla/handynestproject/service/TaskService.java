package codezilla.handynestproject.service;

import codezilla.handynestproject.dto.task.TaskRequestDto;
import codezilla.handynestproject.dto.task.TaskUpdateRequestDto;
import codezilla.handynestproject.model.entity.Performer;
import codezilla.handynestproject.model.entity.Task;

import java.util.List;

public interface TaskService {

    Task createTask(TaskRequestDto dto);
    Task updateTask(TaskUpdateRequestDto dto);
    void deleteTaskById(Long taskId);
    List<Task> getAllTasks();
    Task getTaskById(Long taskId);
    List<Task> getAvailableTasks();
    List<Task> getTasksByUserId(Long userId);
    List<Task> getTasksByPerformerId(Long performerId);
    Task addPerformerToTask(Long taskId, Long performerId);
    Task createTaskByUserId(Long userId, TaskRequestDto dto);




}
