package codezilla.handynestproject.controller;

import codezilla.handynestproject.dto.task.TaskRequestDto;
import codezilla.handynestproject.dto.task.TaskResponseDto;
import codezilla.handynestproject.dto.task.TaskUpdateRequestDto;
import codezilla.handynestproject.model.enums.TaskStatus;
import codezilla.handynestproject.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/task")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
/*POST

TASK ← create

   PUT

TASK ← updateByID

TASK ← addPerformer

TASK ← RemovePerformer

TASK ← updatePublish

TASK ← updateStateByID

   GET

List<TASK> ← findAll

List<TASK> ← findByUserID

List<TASK> ← findAvailable

List<TASK> ← findByPerformerID*/

    @GetMapping
    public List<TaskResponseDto> getAll() {
        return taskService.getAllTasks();
    }

    @GetMapping("/{id}")
    public TaskResponseDto getById(@PathVariable("id") Long id) {
        return taskService.getTaskById(id);
    }

    @GetMapping("/open")
    public List<TaskResponseDto> getAvailable() {
        return taskService.getAvailableTasks();
    }

    @GetMapping("/byUser/{id}")
    public List<TaskResponseDto> getByUserId(@PathVariable Long id) {
        return taskService.getTasksByUserId(id);
    }

    @GetMapping("/byPerformer/{id}")
    public List<TaskResponseDto> getByPerformerId(@PathVariable Long id) {
        return taskService.getTasksByPerformerId(id);
    }

    @GetMapping("/status")
    public List<TaskResponseDto> getByStatus(@RequestParam TaskStatus status) {
        return taskService.getTasksByStatus(status);
    }

    @PostMapping
    public TaskResponseDto create(@RequestBody TaskRequestDto taskRequestDto) {
        return taskService.createTask(taskRequestDto);
    }


    @PutMapping("/update/{id}")
    public TaskResponseDto update(@PathVariable("id") Long id,
                                  @RequestBody TaskUpdateRequestDto taskUpdateRequestDto) {
        return taskService.updateTask(taskUpdateRequestDto);
    }

    @PutMapping("/add/{taskId}/{performerId}")
    public TaskResponseDto addPerformer(@PathVariable("taskId") Long taskId,
                                        @PathVariable("performerId") Long performerId) {
        return taskService.addPerformerToTask(taskId, performerId);
    }

    @PutMapping("/removePerformer/{taskId}")
    public TaskResponseDto removePerformer(@PathVariable Long taskId) {
        return taskService.removePerformerFromTask(taskId);
    }

    @PutMapping("/updateStatus/{taskId}/{status}")
    public TaskResponseDto updateStatus(@PathVariable Long taskId, @PathVariable TaskStatus status) {
        return taskService.updateTaskStatusById(taskId, status);
    }

    @DeleteMapping("/delete/{taskId}")
    public void delete(@PathVariable Long taskId) {
        taskService.deleteTaskById(taskId);
    }

}
