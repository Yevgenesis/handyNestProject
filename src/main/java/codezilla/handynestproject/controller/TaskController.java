package codezilla.handynestproject.controller;

import codezilla.handynestproject.dto.task.TaskRequestDto;
import codezilla.handynestproject.dto.task.TaskResponseDto;
import codezilla.handynestproject.dto.task.TaskUpdateRequestDto;
import codezilla.handynestproject.model.enums.TaskStatus;
import codezilla.handynestproject.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/task")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;


    @GetMapping
    public List<TaskResponseDto> getAllTasks() {
        return taskService.getAllTasks();
    }

    @GetMapping("/{id}")
    public TaskResponseDto getTaskById(@PathVariable("id") Long id) {
        return taskService.getTaskById(id);
    }

    @GetMapping("/open")
    public List<TaskResponseDto> getAvailableTasks() {
        return taskService.getAvailableTasks();
    }

    @GetMapping("/byUser/{id}")
    public List<TaskResponseDto> getTasksByUserId(@PathVariable Long id) {
        return taskService.getTasksByUserId(id);
    }

    @GetMapping("/byPerformer/{id}")
    public List<TaskResponseDto> getTasksByPerformerId(@PathVariable Long id) {
        return taskService.getTasksByPerformerId(id);
    }

    @GetMapping("/status")
    public List<TaskResponseDto> getTasksByStatus(@RequestParam TaskStatus status) {
        return taskService.getTasksByStatus(status);
    }

    @PostMapping
    public TaskResponseDto createTask(@RequestBody TaskRequestDto taskRequestDto) {
        return taskService.createTask(taskRequestDto);
    }

    //TODO такой метод нужен???
    @PostMapping("/create/{id}")
    public TaskResponseDto createTaskByUserId(@PathVariable("id") Long id, @RequestBody TaskRequestDto taskRequestDto) {
        return taskService.createTaskByUserId(id, taskRequestDto);
    }

    @PutMapping("/update/{id}")
    public TaskResponseDto updateTask(@PathVariable("id") Long id,
                                      @RequestBody TaskUpdateRequestDto taskUpdateRequestDto) {
        return taskService.updateTask(taskUpdateRequestDto);
    }

    @PutMapping("/add/{taskId}/{performerId}")
    public TaskResponseDto addPerformerToTask(@PathVariable("taskId") Long taskId,
                                              @PathVariable("performerId") Long performerId) {
        return taskService.addPerformerToTask(taskId, performerId);
    }

    @PutMapping("/removePerformer/{taskId}")
    public TaskResponseDto removePerformerFromTask(@PathVariable Long taskId) {
        return taskService.removePerformerFromTask(taskId);
    }

    @PutMapping("/updateStatus/{taskId}/{status}")
    public TaskResponseDto updateTaskStatus(@PathVariable Long taskId, @PathVariable TaskStatus status) {
        return taskService.updateTaskStatusById(taskId, status);
    }

    @DeleteMapping("/delete/{taskId}")
    public void deleteTask(@PathVariable Long taskId) {
        taskService.deleteTaskById(taskId);
    }

}
