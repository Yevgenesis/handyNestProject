package codezilla.handynestproject.controller;

import codezilla.handynestproject.dto.task.TaskRequestDto;
import codezilla.handynestproject.dto.task.TaskResponseDto;
import codezilla.handynestproject.dto.task.TaskUpdateRequestDto;
import codezilla.handynestproject.mapper.TaskMapper;
import codezilla.handynestproject.model.entity.Task;
import codezilla.handynestproject.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/task")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final TaskMapper taskMapper;

    @GetMapping
    public List<TaskResponseDto> getAllTasks() {
         return taskMapper.toTaskResponseDtoList
                (taskService.getAllTasks());
    }



    @GetMapping("/{id}")
    public TaskResponseDto getTaskById(@PathVariable("id") Long id) {
        return taskMapper.toTaskResponseDto(taskService.getTaskById(id));
    }

    @PostMapping
    public TaskResponseDto createTask(@RequestBody TaskRequestDto taskRequestDto) {
        return taskMapper.toTaskResponseDto(taskService.createTask(taskRequestDto));
    }

    @PostMapping("/update/{id}")
    public TaskResponseDto updateTask(@PathVariable("id") Long id, @RequestBody TaskUpdateRequestDto taskUpdateRequestDto) {
        return taskMapper.toTaskResponseDto(taskService.updateTask(taskUpdateRequestDto));
    }
}
