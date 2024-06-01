package codezilla.handynestproject.controller;

import codezilla.handynestproject.dto.task.TaskRequestDto;
import codezilla.handynestproject.dto.task.TaskResponseDto;
import codezilla.handynestproject.dto.task.TaskUpdateRequestDto;
import codezilla.handynestproject.model.enums.TaskStatus;
import codezilla.handynestproject.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    // POST
    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @PostMapping
    public TaskResponseDto create(@RequestBody @Valid TaskRequestDto taskRequestDto) {
        return taskService.create(taskRequestDto);
    }

    // GET
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping
    public List<TaskResponseDto> findAll() {
        return taskService.findAll();
    }

    @PreAuthorize("hasAnyAuthority('USER','PERFORMER','ADMIN')")
    @GetMapping("/{id}")
    public TaskResponseDto findById(@PathVariable("id") Long id) {
        return taskService.findById(id);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping("/open")
    public List<TaskResponseDto> findAvailable() {
        return taskService.findAvailableTasks();
    }

    // Достать все открытые таски, которые совпадают по категориям для конкретного перформера
    @PreAuthorize("hasAnyAuthority('PERFORMER','ADMIN')")
    @GetMapping("/open/{performerId}")
    public List<TaskResponseDto> findAvailableForPerformer(@PathVariable("performerId") Long performerId) {

        return taskService.findAvailableForPerformer(performerId);
    }

    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @GetMapping("/user/{id}")
    public List<TaskResponseDto> findByUserId(@PathVariable Long id) {
        return taskService.findByUserId(id);
    }

    @PreAuthorize("hasAnyAuthority('PERFORMER','ADMIN')")
    @GetMapping("/performer/{id}")
    public List<TaskResponseDto> findByPerformerId(@PathVariable Long id) {
        return taskService.findByPerformerId(id);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping("/status/{status}")
    public List<TaskResponseDto> findByStatus(@PathVariable TaskStatus status) {
        return taskService.findByStatus(status);
    }

    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    // Достать все таски юзера на которые нужно отправить фитбеки
    @GetMapping("/user/{userId}/unrefereed")
    public List<TaskResponseDto> findUnrefereedByUserId(@PathVariable Long userId) {
        return taskService.findUnrefereedByUserId(userId);
    }

    @PreAuthorize("hasAnyAuthority('PERFORMER','ADMIN')")
    // Достать все таски перформера на которые нужно отправить фитбеки
    @GetMapping("/performer/{performerId}/unrefereed")
    public List<TaskResponseDto> findUnrefereedByPerformerId(@PathVariable Long performerId) {
        return taskService.findUnrefereedByPerformerId(performerId);
    }


    // PUT
    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @PutMapping("/update/{id}")
    public TaskResponseDto update(@PathVariable("id") Long id,
                                  @RequestBody @Valid TaskUpdateRequestDto taskUpdateRequestDto) {
        return taskService.update(taskUpdateRequestDto);
    }

    @PreAuthorize("hasAnyAuthority('PERFORMER','ADMIN')")
    @PutMapping("/{taskId}/addPerformer/{performerId}")
    public TaskResponseDto addPerformer(@PathVariable("taskId") Long taskId,
                                        @PathVariable("performerId") Long performerId) {
        return taskService.addPerformer(taskId, performerId);
    }

    @PreAuthorize("hasAnyAuthority('USER','PERFORMER','ADMIN')")
    @PutMapping("/{taskId}/removePerformer")
    public TaskResponseDto removePerformer(@PathVariable Long taskId) {
        return taskService.removePerformer(taskId);
    }

    @PreAuthorize("hasAnyAuthority('USER','PERFORMER','ADMIN')")
    @PutMapping("/{taskId}/status/{status}")
    public TaskResponseDto updateStatus(@PathVariable Long taskId, @PathVariable TaskStatus status) {
        return taskService.updateStatusById(taskId, status);
    }

    // DELETE
    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @DeleteMapping("/cancel/{taskId}")
    public void cancel(@PathVariable Long taskId) {
        taskService.cancelById(taskId);
    }


}
