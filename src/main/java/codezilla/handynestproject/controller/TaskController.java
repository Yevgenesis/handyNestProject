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
import org.springframework.web.bind.annotation.*;
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
     * post request
     *
     * @param taskRequestDto
     * @return cteated task
     */
    @Operation(summary = "Create a new task",
            description = "Return new task",
            security = @SecurityRequirement(name = "swagger-ui"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Task not  created")
    })
    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @PostMapping
    public TaskResponseDto create(@RequestBody @Valid TaskRequestDto taskRequestDto) {
        log.info("Create task: {}", taskRequestDto);
        return taskService.create(taskRequestDto);
    }

    /**
     * get request
     *
     * @return all tasks
     */
    @Operation(summary = "Find all tasks",
            description = "Return all tasks",
            security = @SecurityRequirement(name = "swagger-ui"))
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
     * get request
     *
     * @param id
     * @return task by id
     */
    @Operation(summary = "Find task by id",
            description = "Return task by id",
            security = @SecurityRequirement(name = "swagger-ui"))
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
     * get request
     *
     * @return all opened tasks
     */
    @Operation(summary = "Find all opened tasks",
            description = "Return all opened tasks",
            security = @SecurityRequirement(name = "swagger-ui"))
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


        // Достать все открытые таски, которые совпадают по категориям для конкретного перформера
        /**
         * get request
         * @param performerId
         * @return all the open tasks that match the categories for a particular performer
         */
        @Operation(summary = "Find all tasks that match the categories for a particular performer",
                description = "Return all tasks that match the categories for a particular performer",
                security = @SecurityRequirement(name = "swagger-ui"))
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
                @ApiResponse(responseCode = "404", description = "Performer not found")
        })
        @PreAuthorize("hasAnyAuthority('PERFORMER','ADMIN')")
        @GetMapping("/open/{performerId}")
        public List<TaskResponseDto> findAvailableForPerformer (@PathVariable("performerId") Long performerId){
            log.info("Find available tasks for performer: {}", performerId);
            return taskService.findAvailableForPerformer(performerId);
        }


        /**
         * get request
         * @param id
         * @return available tasks of a particular user
         */
        @Operation(summary = "Find all tasks for a particular user",
                description = "Return all for a particular user",
                security = @SecurityRequirement(name = "swagger-ui"))
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
                @ApiResponse(responseCode = "404", description = "User not found")
        })
        @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
        @GetMapping("/user/{id}")
        public List<TaskResponseDto> findByUserId (@PathVariable Long id){
            log.info("Find all tasks for user: {}", id);
            return taskService.findByUserId(id);
        }

        // Достать все таски перформера (со всеми статусами)
        /**
         * get request
         * @param id
         * @return all tasks of a particular performer
         */

        @Operation(summary = "Find all tasks a particular performer",
                description = "Return all tasks a particular performer",
                security = @SecurityRequirement(name = "swagger-ui"))
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
                @ApiResponse(responseCode = "404", description = "Performer not found")
        })
        @PreAuthorize("hasAnyAuthority('PERFORMER','ADMIN')")
        @GetMapping("/performer/{id}")
        public List<TaskResponseDto> findAllByPerformerId (@PathVariable Long id){
            log.info("Find all tasks for performer: {}", id);
            return taskService.findAllByPerformerId(id);
        }

            /**
             * get request
             * @param status
             * @return all tasks with status from param
             */

            @Operation(summary = "Find all tasks with particular status",
                    description = "Return all tasks with particular status",
                    security = @SecurityRequirement(name = "swagger-ui"))
            @ApiResponses(value = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
                    @ApiResponse(responseCode = "404", description = "Tasks not found")
            })
            @PreAuthorize("hasAnyAuthority('ADMIN')")
            @GetMapping("/status/{status}")
            public List<TaskResponseDto> findByStatus (@PathVariable TaskStatus status){
                log.info("Find all tasks for status: {}", status);
                return taskService.findByStatus(status);
            }

            /**
             * get request
             * @param userId
             * @return all the tasks of the user to which need to send feedbacks
             */
            @Operation(summary = "Find all the tasks of the user to which need to send feedbacks",
                    description = "Return all the tasks of the user to which need to send feedbacks",
                    security = @SecurityRequirement(name = "swagger-ui"))
            @ApiResponses(value = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
                    @ApiResponse(responseCode = "404", description = "Tasks not found")
            })
            @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
            @GetMapping("/user/{userId}/unrefereed")
            public List<TaskResponseDto> findUnrefereedByUserId (@PathVariable Long userId){
                log.info("Find all tasks for unrefereed by user id: {}", userId);
                return taskService.findUnrefereedByUserId(userId);
            }

            /**
             * get request
             * @param performerId
             * @return all the tasks of the performer to which need to send feedbacks
             */
            @Operation(summary = "Find all the tasks of the performer to which need to send feedbacks",
                    description = "Return all the tasks of the performer to which need to send feedbacks",
                    security = @SecurityRequirement(name = "swagger-ui"))
            @ApiResponses(value = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
                    @ApiResponse(responseCode = "404", description = "Tasks not found")
            })
            @PreAuthorize("hasAnyAuthority('PERFORMER','ADMIN')")
            @GetMapping("/performer/{performerId}/unrefereed")
            public List<TaskResponseDto> findUnrefereedByPerformerId (@PathVariable Long performerId){
                log.info("Find all tasks for unrefereed by performer id: {}", performerId);
                return taskService.findUnrefereedByPerformerId(performerId);
            }

            /**
             * put request
             * @param id
             * @param taskUpdateRequestDto
             * @return task with updated data
             */
            @Operation(summary = "Update task data",
                    description = "Return task with updated data",
                    security = @SecurityRequirement(name = "swagger-ui"))
            @ApiResponses(value = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
                    @ApiResponse(responseCode = "404", description = "Tasks not found")
            })
            @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
            @PutMapping("/update/{id}")
            public TaskResponseDto update (@PathVariable("id") Long id,
                    @RequestBody @Valid TaskUpdateRequestDto taskUpdateRequestDto){
                log.info("Update task: {}", taskUpdateRequestDto);
                return taskService.update(taskUpdateRequestDto);
            }

            /**
             * put request
             * @param taskId
             * @param performerId
             * @return task with added performer
             */
            @Operation(summary = "Add performer to the task",
                    description = "Return task with performer",
                    security = @SecurityRequirement(name = "swagger-ui"))
            @ApiResponses(value = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
                    @ApiResponse(responseCode = "404", description = "Tasks not found")
            })
            @PreAuthorize("hasAnyAuthority('PERFORMER','ADMIN')")
            @PutMapping("/{taskId}/addPerformer/{performerId}")
            public TaskResponseDto addPerformer (@PathVariable("taskId") Long taskId,
                    @PathVariable("performerId") Long performerId){
                log.info("Add performer: {}", performerId);
                return taskService.addPerformer(taskId, performerId);
            }

            /**
             * put request
             * @param taskId
             * @return task without performer
             */
            @Operation(summary = "Remove performer from the task",
                    description = "Return task without performer",
                    security = @SecurityRequirement(name = "swagger-ui"))
            @ApiResponses(value = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
                    @ApiResponse(responseCode = "404", description = "Tasks not found")
            })
            @PreAuthorize("hasAnyAuthority('USER','PERFORMER','ADMIN')")
            @PutMapping("/{taskId}/removePerformer")
            public TaskResponseDto removePerformer (@PathVariable Long taskId){
                log.info("Remove performer: {}", taskId);
                return taskService.removePerformer(taskId);
            }

            /**
             * put request
             * @param taskId
             * @param status
             * @return task with status from param
             */
            @Operation(summary = "Update task status",
                    description = "Return task with new status",
                    security = @SecurityRequirement(name = "swagger-ui"))
            @ApiResponses(value = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
                    @ApiResponse(responseCode = "404", description = "Tasks not found")
            })
            @PreAuthorize("hasAnyAuthority('USER','PERFORMER','ADMIN')")
            @PutMapping("/{taskId}/status/{status}")
            public TaskResponseDto updateStatus (@PathVariable Long taskId, @PathVariable TaskStatus status){
                log.info("Update task status: {}", status);
                return taskService.updateStatusById(taskId, status);
            }

            /**
             * delete request
             * @param taskId
             */
            @Operation(summary = "Canceled the task",
                    description = "Return task with status canceled",
                    security = @SecurityRequirement(name = "swagger-ui"))
            @ApiResponses(value = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
                    @ApiResponse(responseCode = "404", description = "Tasks not found")
            })
            @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
            @DeleteMapping("/cancel/{taskId}")
            public void cancel (@PathVariable Long taskId){
                log.info("Cancel task: {}", taskId);
                taskService.cancelById(taskId);
            }


        }

