package codezilla.handynestproject.service;

import codezilla.handynestproject.dto.task.TaskRequestDto;
import codezilla.handynestproject.dto.task.TaskUpdateRequestDto;
import codezilla.handynestproject.model.entity.Task;

import java.util.List;

public interface TaskService {

    Task createTask(TaskRequestDto dto);
    Task updateTask(TaskUpdateRequestDto dto);
    void deleteTaskById(Long taskId);
    List<Task> getAllTasks();
    Task getTaskById(Long taskId);
    List<Task> getAllPublishTasks();
//    Task addPerformer(Performer performer);
//





}
