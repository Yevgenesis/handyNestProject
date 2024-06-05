package codezilla.handynestproject.controller;

import codezilla.handynestproject.dto.task.TaskRequestDto;
import codezilla.handynestproject.dto.task.TaskResponseDto;
import codezilla.handynestproject.dto.task.TaskUpdateRequestDto;
import codezilla.handynestproject.model.enums.TaskStatus;
import codezilla.handynestproject.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@RestController
@RequestMapping(path = "/tasks")
@RequiredArgsConstructor
@Tag(name = "Task Controller", description = "Operations related to tasks")
public class TaskController {

    private final TaskService taskService;

    /**
     * Creates a new task.
     *
     * @param taskRequestDto the task request DTO
     * @return the created task response DTO
     */
    @Operation(summary = "Create a new task", description = "Return new task",
            security = @SecurityRequirement(name = "tasks"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully created"),
            @ApiResponse(responseCode = "404", description = "Task not created")
    })
    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @PostMapping
    public TaskResponseDto create(@RequestBody @Valid TaskRequestDto taskRequestDto) {
        log.info("Create task: {}", taskRequestDto);
        return taskService.create(taskRequestDto);
    }

    /**
     * Finds all tasks.
     *
     * @return the list of all tasks
     */
    @Operation(summary = "Find all tasks", description = "Return all tasks",
            security = @SecurityRequirement(name = "tasks"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping
    public List<TaskResponseDto> findAll() {
        log.info("Find all tasks");
        return taskService.findAll();
    }

    /**
     * Finds a task by ID.
     *
     * @param id the ID of the task
     * @return the task response DTO
     */
    @Operation(summary = "Find task by id", description = "Return task by id",
            security = @SecurityRequirement(name = "tasks"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @PreAuthorize("hasAnyAuthority('USER','PERFORMER','ADMIN')")
    @GetMapping("/{id}")
    public TaskResponseDto findById(@PathVariable("id") Long id) {
        log.info("Find task by id: {}", id);
        return taskService.findById(id);
    }

    /**
     * Finds all opened tasks.
     *
     * @return the list of all opened tasks
     */
    @Operation(summary = "Find all opened tasks", description = "Return all opened tasks",
            security = @SecurityRequirement(name = "tasks"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Tasks not found")
    })
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping("/open")
    public List<TaskResponseDto> findAvailable() {
        log.info("Find available tasks");
        return taskService.findAvailableTasks();
    }


    /**
     * Finds all opened tasks that match the categories for a particular performer.
     *
     * @param performerId the ID of the performer
     * @return the list of tasks that match the categories for the performer
     */
    @Operation(summary = "Find all tasks that match the categories for a particular performer",
            description = "Return all tasks that match the categories for a particular performer",
            security = @SecurityRequirement(name = "tasks"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Performer not found")
    })
    @PreAuthorize("hasAnyAuthority('PERFORMER','ADMIN')")
    @GetMapping("/open/{performerId}")
    public List<TaskResponseDto> findAvailableForPerformer(@PathVariable("performerId") Long performerId) {
        log.info("Find available tasks for performer: {}", performerId);
        return taskService.findAvailableForPerformer(performerId);
    }


    /**
     * Finds all tasks for a particular user.
     *
     * @param id the ID of the user
     * @return the list of tasks for the user
     */
    @Operation(summary = "Find all tasks for a particular user",
            description = "Return all tasks for a particular user",
            security = @SecurityRequirement(name = "tasks"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @GetMapping("/user/{id}")
    public List<TaskResponseDto> findByUserId(@PathVariable Long id) {
        log.info("Find all tasks for user: {}", id);
        return taskService.findByUserId(id);
    }

    /**
     * Finds all tasks for a particular performer.
     *
     * @param id the ID of the performer
     * @return the list of tasks for the performer
     */
    @Operation(summary = "Find all tasks for a particular performer",
            description = "Return all tasks for a particular performer",
            security = @SecurityRequirement(name = "tasks"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Performer not found")
    })
    @PreAuthorize("hasAnyAuthority('PERFORMER','ADMIN')")
    @GetMapping("/performer/{id}")
    public List<TaskResponseDto> findAllByPerformerId(@PathVariable Long id) {
        log.info("Find all tasks for performer: {}", id);
        return taskService.findAllByPerformerId(id);
    }

    /**
     * Finds all tasks with a particular status.
     *
     * @param status the status of the tasks
     * @return the list of tasks with the specified status
     */
    @Operation(summary = "Find all tasks with particular status",
            description = "Return all tasks with particular status",
            security = @SecurityRequirement(name = "tasks"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Tasks not found")
    })
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping("/status/{status}")
    public List<TaskResponseDto> findByStatus(@PathVariable TaskStatus status) {
        log.info("Find all tasks for status: {}", status);
        return taskService.findByStatus(status);
    }

    /**
     * Finds all tasks of the user that need feedback.
     *
     * @param userId the ID of the user
     * @return the list of tasks of the user that need feedback
     */
    @Operation(summary = "Find all tasks of the user that need feedback",
            description = "Return all tasks of the user that need feedback",
            security = @SecurityRequirement(name = "tasks"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Tasks not found")
    })
    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @GetMapping("/user/{userId}/unrefereed")
    public List<TaskResponseDto> findUnrefereedByUserId(@PathVariable Long userId) {
        log.info("Find all tasks for unrefereed by user id: {}", userId);
        return taskService.findUnrefereedByUserId(userId);
    }

    /**
     * Finds all tasks of the performer that need feedback.
     *
     * @param performerId the ID of the performer
     * @return the list of tasks of the performer that need feedback
     */
    @Operation(summary = "Find all tasks of the performer that need feedback",
            description = "Return all tasks of the performer that need feedback",
            security = @SecurityRequirement(name = "tasks"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Tasks not found")
    })
    @PreAuthorize("hasAnyAuthority('PERFORMER','ADMIN')")
    @GetMapping("/performer/{performerId}/unrefereed")
    public List<TaskResponseDto> findUnrefereedByPerformerId(@PathVariable Long performerId) {
        log.info("Find all tasks for unrefereed by performer id: {}", performerId);
        return taskService.findUnrefereedByPerformerId(performerId);
    }

    /**
     * Updates task data.
     *
     * @param id the ID of the task
     * @param taskUpdateRequestDto the task update request DTO
     * @return the updated task response DTO
     */
    @Operation(summary = "Update task data", description = "Return task with updated data",
            security = @SecurityRequirement(name = "tasks"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Tasks not found")
    })
    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @PutMapping("/update/{id}")
    public TaskResponseDto update(@PathVariable("id") Long id,
                                  @RequestBody @Valid TaskUpdateRequestDto taskUpdateRequestDto) {
        log.info("Update task: {}", taskUpdateRequestDto);
        return taskService.update(taskUpdateRequestDto);
    }

    /**
     * Adds a performer to the task.
     *
     * @param taskId the ID of the task
     * @param performerId the ID of the performer
     * @return the task response DTO with the added performer
     */
    @Operation(summary = "Add performer to the task", description = "Return task with performer",
            security = @SecurityRequirement(name = "tasks"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Tasks not found")
    })
    @PreAuthorize("hasAnyAuthority('PERFORMER','ADMIN')")
    @PutMapping("/{taskId}/addPerformer/{performerId}")
    public TaskResponseDto addPerformer(@PathVariable("taskId") Long taskId,
                                        @PathVariable("performerId") Long performerId) {
        log.info("Add performer: {}", performerId);
        return taskService.addPerformer(taskId, performerId);
    }

    /**
     * Removes a performer from the task.
     *
     * @param taskId the ID of the task
     * @return the task response DTO without the performer
     */
    @Operation(summary = "Remove performer from the task",
            description = "Return task without performer",
            security = @SecurityRequirement(name = "tasks"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Tasks not found")
    })
    @PreAuthorize("hasAnyAuthority('USER','PERFORMER','ADMIN')")
    @PutMapping("/{taskId}/removePerformer")
    public TaskResponseDto removePerformer(@PathVariable Long taskId) {
        log.info("Remove performer: {}", taskId);
        return taskService.removePerformer(taskId);
    }

    /**
     * Updates the status of a task.
     *
     * @param taskId the ID of the task
     * @param status the new status of the task
     * @return the task response DTO with the updated status
     */
    @Operation(summary = "Update task status", description = "Return task with new status",
            security = @SecurityRequirement(name = "tasks"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Tasks not found")
    })
    @PreAuthorize("hasAnyAuthority('USER','PERFORMER','ADMIN')")
    @PutMapping("/{taskId}/status/{status}")
    public TaskResponseDto updateStatus(@PathVariable Long taskId, @PathVariable TaskStatus status) {
        log.info("Update task status: {}", status);
        return taskService.updateStatusById(taskId, status);
    }

    /**
     * Cancels a task.
     *
     * @param taskId the ID of the task
     */
    @Operation(summary = "Cancel the task", description = "Return task with status canceled",
            security = @SecurityRequirement(name = "tasks"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Tasks not found")
    })
    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @DeleteMapping("/cancel/{taskId}")
    public void cancel(@PathVariable Long taskId) {
        log.info("Cancel task: {}", taskId);
        taskService.cancelById(taskId);
    }


}

