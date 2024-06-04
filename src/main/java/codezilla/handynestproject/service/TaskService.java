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

    TaskResponseDto findById(Long taskId);

    List<TaskResponseDto> findAll();

    List<TaskResponseDto> findByStatus(TaskStatus status);

    Task findTaskEntityByIdAndParticipantsId(Long taskId, Long userId);

    List<TaskResponseDto> findAvailableTasks();

    List<TaskResponseDto> findByUserId(Long userId);

    List<TaskResponseDto> findAllByPerformerId(Long performerId);

    TaskResponseDto addPerformer(Long taskId, Long performerId);

    TaskResponseDto removePerformer(Long taskId);

    TaskResponseDto updateStatusById(Long taskId, TaskStatus status);


    List<TaskResponseDto> findUnrefereedByUserId(Long userId);

    List<TaskResponseDto> findUnrefereedByPerformerId(Long performerId);

    List<TaskResponseDto> findAvailableForPerformer(Long performerId);
}
