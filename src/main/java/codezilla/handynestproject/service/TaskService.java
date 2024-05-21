package codezilla.handynestproject.service;

import codezilla.handynestproject.dto.task.TaskRequestDto;
import codezilla.handynestproject.dto.task.TaskResponseDto;
import codezilla.handynestproject.dto.task.TaskUpdateRequestDto;
import codezilla.handynestproject.model.entity.Task;
import codezilla.handynestproject.model.enums.TaskStatus;

import java.util.List;

public interface TaskService {

    TaskResponseDto createTask(TaskRequestDto dto);
    TaskResponseDto updateTask(TaskUpdateRequestDto dto);
    void deleteTaskById(Long taskId);
    TaskResponseDto getTaskById(Long taskId);
    List<TaskResponseDto> getAllTasks();
    List<TaskResponseDto> getTasksByStatus(TaskStatus status);

    Task getTaskEntityByIdAndParticipantsId(Long taskId, Long userId);

    List<TaskResponseDto> getAvailableTasks();

    List<TaskResponseDto> getTasksByUserId(Long userId);
    List<TaskResponseDto> getTasksByPerformerId(Long performerId);
    TaskResponseDto addPerformerToTask(Long taskId, Long performerId);
    TaskResponseDto removePerformerFromTask(Long taskId);
    TaskResponseDto updateTaskStatusById(Long taskId, TaskStatus status);
    TaskResponseDto createTaskByUserId(Long userId, TaskRequestDto dto);




}
