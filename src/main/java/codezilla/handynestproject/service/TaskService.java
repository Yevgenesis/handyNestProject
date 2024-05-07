package codezilla.handynestproject.service;

import codezilla.handynestproject.dto.task.TaskRequestDto;
import codezilla.handynestproject.dto.task.TaskResponseDto;
import codezilla.handynestproject.dto.task.TaskUpdateRequestDto;

import java.util.List;

public interface TaskService {

    TaskResponseDto createTask(TaskRequestDto dto);
    TaskResponseDto updateTask(TaskUpdateRequestDto dto);
    void deleteTaskById(Long taskId);

    List<TaskResponseDto> getAllTasks();

    TaskResponseDto getTaskById(Long taskId);

    List<TaskResponseDto> getAvailableTasks();

    List<TaskResponseDto> getTasksByUserId(Long userId);
    List<TaskResponseDto> getTasksByPerformerId(Long performerId);
    TaskResponseDto addPerformerToTask(Long taskId, Long performerId);
    TaskResponseDto createTaskByUserId(Long userId, TaskRequestDto dto);




}
