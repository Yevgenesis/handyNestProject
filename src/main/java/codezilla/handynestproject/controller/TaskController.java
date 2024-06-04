package codezilla.handynestproject.controller;

import codezilla.handynestproject.dto.task.TaskRequestDto;
import codezilla.handynestproject.dto.task.TaskResponseDto;
import codezilla.handynestproject.dto.task.TaskUpdateRequestDto;
import codezilla.handynestproject.model.enums.TaskStatus;
import codezilla.handynestproject.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    // POST
    @PostMapping
    public TaskResponseDto create(@RequestBody @Valid TaskRequestDto taskRequestDto) {
        return taskService.create(taskRequestDto);
    }

    // GET
    @GetMapping
    public List<TaskResponseDto> findAll() {
        return taskService.findAll();
    }

    @GetMapping("/{id}")
    public TaskResponseDto findById(@PathVariable("id") Long id) {
        return taskService.findById(id);
    }

    @GetMapping("/open")
    public List<TaskResponseDto> findAllAvailable() {
        return taskService.findAvailableTasks();
    }

    // Достать все открытые таски, которые совпадают по категориям для конкретного перформера
    @GetMapping("/open/{performerId}")
    public List<TaskResponseDto> findAvailableForPerformer(@PathVariable("performerId") Long performerId) {

        return taskService.findAvailableForPerformer(performerId);
    }

    @GetMapping("/user/{id}")
    public List<TaskResponseDto> findByUserId(@PathVariable Long id) {
        return taskService.findByUserId(id);
    }

    // Достать все таски перформера (со всеми статусами)
    @GetMapping("/performer/{id}")
    public List<TaskResponseDto> findAllByPerformerId(@PathVariable Long id) {
        return taskService.findAllByPerformerId(id);
    }

    @GetMapping("/status/{status}")
    public List<TaskResponseDto> findByStatus(@PathVariable TaskStatus status) {
        return taskService.findByStatus(status);
    }

    // Достать все таски юзера на которые нужно отправить фитбеки
    @GetMapping("/user/{userId}/unrefereed")
    public List<TaskResponseDto> findUnrefereedByUserId(@PathVariable Long userId) {
        return taskService.findUnrefereedByUserId(userId);
    }

    // Достать все таски перформера на которые нужно отправить фитбеки
    @GetMapping("/performer/{performerId}/unrefereed")
    public List<TaskResponseDto> findUnrefereedByPerformerId(@PathVariable Long performerId) {
        return taskService.findUnrefereedByPerformerId(performerId);
    }


    // PUT
    @PutMapping("/update/{id}")
    public TaskResponseDto update(@PathVariable("id") Long id,
                                  @RequestBody @Valid TaskUpdateRequestDto taskUpdateRequestDto) {
        return taskService.update(taskUpdateRequestDto);
    }

    @PutMapping("/{taskId}/addPerformer/{performerId}")
    public TaskResponseDto addPerformer(@PathVariable("taskId") Long taskId,
                                        @PathVariable("performerId") Long performerId) {
        return taskService.addPerformer(taskId, performerId);
    }

    @PutMapping("/{taskId}/removePerformer")
    public TaskResponseDto removePerformer(@PathVariable Long taskId) {
        return taskService.removePerformer(taskId);
    }

    @PutMapping("/{taskId}/status/{status}")
    public TaskResponseDto updateStatus(@PathVariable Long taskId, @PathVariable TaskStatus status) {
        return taskService.updateStatusById(taskId, status);
    }

    // DELETE
    @DeleteMapping("/cancel/{taskId}")
    public void cancel(@PathVariable Long taskId) {
        taskService.cancelById(taskId);
    }


}
