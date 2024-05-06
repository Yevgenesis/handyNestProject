package codezilla.handynestproject.service;

import codezilla.handynestproject.dto.task.TaskRequestDto;
import codezilla.handynestproject.dto.task.TaskResponseDto;
import codezilla.handynestproject.dto.task.TaskUpdateRequestDto;
import codezilla.handynestproject.model.entity.Task;

import java.util.List;

public interface TaskService {

    Task createTask(TaskRequestDto dto);
    Task updateTask(TaskUpdateRequestDto dto);
    void deleteTaskById(Long taskId);

    List<TaskResponseDto> getAllTasks();

    TaskResponseDto getTaskById(Long taskId);

    List<TaskResponseDto> getAvailableTasks();

    List<TaskResponseDto> getTasksByUserId(Long userId);
    List<Task> getTasksByPerformerId(Long performerId);
    Task addPerformerToTask(Long taskId, Long performerId);
    Task createTaskByUserId(Long userId, TaskRequestDto dto);




}
