package codezilla.handynestproject.service;

import codezilla.handynestproject.dto.task.TaskRequestDto;
import codezilla.handynestproject.model.entity.Task;

import java.util.UUID;

public interface TaskService {

    Task createTask(TaskRequestDto dto);
    Task updateTask(TaskRequestDto dto);
    void deleteTaskById(Long taskId);





}
