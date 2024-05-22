package codezilla.handynestproject.service;

import codezilla.handynestproject.dto.task.TaskRequestDto;
import codezilla.handynestproject.dto.task.TaskResponseDto;
import codezilla.handynestproject.dto.task.TaskUpdateRequestDto;
import codezilla.handynestproject.model.entity.Task;
import codezilla.handynestproject.model.enums.TaskStatus;

import java.util.List;

public interface TaskService {

    TaskResponseDto create(TaskRequestDto dto);
    TaskResponseDto update(TaskUpdateRequestDto dto);
    void cancelById(Long taskId);
    TaskResponseDto getById(Long taskId);
    List<TaskResponseDto> getAll();
    List<TaskResponseDto> getByStatus(TaskStatus status);

    Task getTaskEntityByIdAndParticipantsId(Long taskId, Long userId);

    List<TaskResponseDto> getAvailableTasks();

    List<TaskResponseDto> getByUserId(Long userId);
    List<TaskResponseDto> getByPerformerId(Long performerId);
    TaskResponseDto addPerformer(Long taskId, Long performerId);
    TaskResponseDto removePerformer(Long taskId);
    TaskResponseDto updateTaskStatusById(Long taskId, TaskStatus status);




}
