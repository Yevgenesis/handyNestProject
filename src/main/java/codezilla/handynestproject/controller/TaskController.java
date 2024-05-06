package codezilla.handynestproject.controller;

import codezilla.handynestproject.dto.task.TaskRequestDto;
import codezilla.handynestproject.dto.task.TaskResponseDto;
import codezilla.handynestproject.dto.task.TaskUpdateRequestDto;
import codezilla.handynestproject.dto.task.TaskWithPerformerResponseDto;
import codezilla.handynestproject.mapper.TaskMapper;
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
        List<TaskResponseDto> dtos = taskService.getAllTasks();
        return dtos;
    }
    @GetMapping("/open")
    public List<TaskResponseDto> getAvailableTasks() {
        return taskService.getAvailableTasks();
    }

    @GetMapping("/byUser/{id}")
    public List<TaskResponseDto> getTasksByUser(@PathVariable Long id) {
        return taskService.getTasksByUserId(id);
    }

    @GetMapping("/byPerformer/{id}")
    public List<TaskResponseDto> getTasksByPerformerId(@PathVariable Long id) {
        return taskMapper.toTaskResponseDtoList(taskService.getTasksByPerformerId(id));
    }

    @GetMapping("/{id}")
    public TaskResponseDto getTaskById(@PathVariable("id") Long id) {
        return taskService.getTaskById(id);
    }

    // ToDo - BAG с WorkingTime не добавляет в базу, если в поле working_time_id одинаковое ID
    @PostMapping
    public TaskResponseDto createTask(@RequestBody TaskRequestDto taskRequestDto) {
        return taskMapper.toTaskResponseDto(taskService.createTask(taskRequestDto));
    }

    @PostMapping("/create/{id}")
    public TaskResponseDto createTaskByUserId(@PathVariable("id") Long id, @RequestBody TaskRequestDto taskRequestDto) {
        return taskMapper.toTaskResponseDto(taskService.createTaskByUserId(id, taskRequestDto));
    }

    @PutMapping("/update/{id}")
    public TaskResponseDto updateTask(@PathVariable("id") Long id, @RequestBody TaskUpdateRequestDto taskUpdateRequestDto) {
        return taskMapper.toTaskResponseDto(taskService.updateTask(taskUpdateRequestDto));
    }

    @PutMapping("/add/{taskId}/{performerId}")
    public TaskWithPerformerResponseDto addPerformerToTask(@PathVariable("taskId") Long taskId,
                                                           @PathVariable("performerId") Long performerId) {
        return taskMapper.toTaskWithPerformerResponseDto(taskService.addPerformerToTask(taskId, performerId));
    }
}
