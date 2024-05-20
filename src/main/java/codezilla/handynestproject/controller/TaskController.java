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
    public List<TaskResponseDto> getAll() {
        return taskService.getAll();
    }

    @GetMapping("/{id}")
    public TaskResponseDto getById(@PathVariable("id") Long id) {
        return taskService.getById(id);
    }

    @GetMapping("/open")
    public List<TaskResponseDto> getAvailable() {
        return taskService.getAvailableTasks();
    }

    @GetMapping("/byUser/{id}")
    public List<TaskResponseDto> getByUserId(@PathVariable Long id) {
        return taskService.getByUserId(id);
    }

    @GetMapping("/byPerformer/{id}")
    public List<TaskResponseDto> getByPerformerId(@PathVariable Long id) {
        return taskService.getByPerformerId(id);
    }

    @GetMapping("/status")
    public List<TaskResponseDto> getByStatus(@RequestParam TaskStatus status) {
        return taskService.getByStatus(status);
    }

    @PostMapping
    public TaskResponseDto create(@RequestBody TaskRequestDto taskRequestDto) {
        return taskService.create(taskRequestDto);
    }


    @PutMapping("/update/{id}")
    public TaskResponseDto update(@PathVariable("id") Long id,
                                  @RequestBody TaskUpdateRequestDto taskUpdateRequestDto) {
        return taskService.update(taskUpdateRequestDto);
    }

    @PutMapping("/add/{taskId}/{performerId}")
    public TaskResponseDto addPerformer(@PathVariable("taskId") Long taskId,
                                        @PathVariable("performerId") Long performerId) {
        return taskService.addPerformer(taskId, performerId);
    }

    @PutMapping("/removePerformer/{taskId}")
    public TaskResponseDto removePerformer(@PathVariable Long taskId) {
        return taskService.removePerformer(taskId);
    }

    @PutMapping("/updateStatus/{taskId}/{status}")
    public TaskResponseDto updateTaskStatus(@PathVariable Long taskId, @PathVariable TaskStatus status) {
        return taskService.updateTaskStatusById(taskId, status);
    }

    @DeleteMapping("/cancel/{taskId}")
    public void cancel(@PathVariable Long taskId) {
        taskService.cancelById(taskId);
    }

}
