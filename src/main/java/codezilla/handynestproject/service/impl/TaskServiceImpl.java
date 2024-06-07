package codezilla.handynestproject.service.impl;

import codezilla.handynestproject.dto.task.TaskRequestDto;
import codezilla.handynestproject.dto.task.TaskResponseDto;
import codezilla.handynestproject.dto.task.TaskUpdateRequestDto;
import codezilla.handynestproject.exception.PerformerNotFoundException;
import codezilla.handynestproject.exception.TaskNotFoundException;
import codezilla.handynestproject.exception.TaskWrongStatusException;
import codezilla.handynestproject.exception.UserAccessDeniedException;
import codezilla.handynestproject.exception.UserNotFoundException;
import codezilla.handynestproject.mapper.AddressMapper;
import codezilla.handynestproject.mapper.TaskMapper;
import codezilla.handynestproject.model.entity.Address;
import codezilla.handynestproject.model.entity.Chat;
import codezilla.handynestproject.model.entity.Performer;
import codezilla.handynestproject.model.entity.Task;
import codezilla.handynestproject.model.enums.TaskStatus;
import codezilla.handynestproject.repository.TaskRepository;
import codezilla.handynestproject.service.CategoryService;
import codezilla.handynestproject.service.PerformerService;
import codezilla.handynestproject.service.TaskService;
import codezilla.handynestproject.service.UserService;
import codezilla.handynestproject.service.WorkingTimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Implementation of the TaskService interface.
 */

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final UserService userService;
    private final WorkingTimeService workingTimeService;
    private final CategoryService categoryService;
    private final PerformerService performerService;
    private final TaskMapper taskMapper;
    private final AddressMapper addressMapper;

    /**
     * Create a new task based on the provided TaskRequestDto.
     *
     * @param dto The TaskRequestDto containing task details
     * @return The TaskResponseDto of the created task
     */
    @Override
    @Transactional
    public TaskResponseDto create(TaskRequestDto dto) {

        Address address = dto.address() != null ? addressMapper.dtoToAddress(dto.address()) : null;
        Task task = Task.builder()
                .title(dto.title())
                .description(dto.description())
                .price(dto.price())
                .address(address)
                .taskStatus(TaskStatus.OPEN)
                .isPublish(dto.isPublish())
                .workingTime(workingTimeService.findWorkingTimeById(dto.workingTimeId()))
                .category(categoryService.findById(dto.categoryId()))
                .user(userService.findByIdReturnUser(dto.userId()))
                .build();

        return taskMapper.toTaskResponseDto(taskRepository.save(task));
    }

    /**
     * Update an existing task based on the provided TaskUpdateRequestDto.
     *
     * @param dto The TaskUpdateRequestDto containing updated task details
     * @return The TaskResponseDto of the updated task
     * @throws TaskNotFoundException when task not found
     */
    @Override
    @Transactional
    public TaskResponseDto update(TaskUpdateRequestDto dto) {
        Long workingTimeId = dto.getWorkingTimeId();

        Task task = taskRepository.findById(dto.getId())
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));

        if (!task.getTaskStatus().equals(TaskStatus.OPEN))
            throw new TaskNotFoundException(String.format("Task with ID %s has status: %s and cannot be updated", task.getId(), task.getTaskStatus()));

        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setPrice(dto.getPrice());
        task.setAddress(addressMapper.dtoToAddress(dto.getAddressDto()));
        task.setWorkingTime(workingTimeService.findWorkingTimeById(workingTimeId));

        return taskMapper.toTaskResponseDto(taskRepository.save(task));
    }

    /**
     * Cancel a task by its ID.
     *
     * @param taskId The ID of the task to cancel
     * @throws TaskNotFoundException when task not found
     */
    @Override
    public void cancelById(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task with id:" + taskId + " not found"));
        Set<Chat> chats = task.getChats();
        for (Chat chat : chats) {
            chat.setDeleted(true);
        }
        task.setChats(chats);
        task.setTaskStatus(TaskStatus.CANCELED);
        taskRepository.save(task);
    }

    /**
     * Find all tasks.
     *
     * @return List of all tasks
     * @throws TaskNotFoundException when task not found
     */
    @Override
    @Transactional
    public List<TaskResponseDto> findAll() {
        List<Task> tasks = taskRepository.findAll();
        return taskMapper.toTaskResponseDtoList(tasks);
    }

    /**
     * Find a task by its ID.
     *
     * @param taskId The ID of the task to find
     * @return The TaskResponseDto of the found task
     * @throws TaskNotFoundException when task not found
     */
    @Override
    @Transactional
    public TaskResponseDto findById(Long taskId) {
        Optional<Task> task = Optional.of(taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Not found task with id: " + taskId)));
        return taskMapper.toTaskResponseDto(task.get());
    }

    /**
     * Check if a task exists by its ID.
     *
     * @param taskId The ID of the task to check
     * @return True if the task exists, false otherwise
     */
    @Override
    public boolean existsById(Long taskId) {
        return taskRepository.existsById(taskId);
    }

    /**
     * Find a task entity by task id and participant id.
     *
     * @param taskId The id of the task to find
     * @param userId The id of the participant to find
     * @return The task entity found
     * @throws TaskNotFoundException when task not found
     */
    @Override
    public Task findTaskEntityByIdAndParticipantsId(Long taskId, Long userId) {

        Optional<Task> task = Optional.of(taskRepository
                .findTaskByIdAndStatusIsNotOPENAndPerformerOrUser(taskId, userId)
                .orElseThrow(() -> new TaskNotFoundException(String
                        .format("Not found taskID: %s with participantID: %s",taskId,userId))));
        return task.get();
    }

    /**
     * Find all available tasks with status OPEN.
     *
     * @return List of available tasks
     */
    @Override
    @Transactional
    public List<TaskResponseDto> findAvailableTasks() {
        List<Task> tasks = taskRepository.findTaskByTaskStatus(TaskStatus.OPEN);
        return taskMapper.toTaskResponseDtoList(tasks);
    }

    /**
     * Find all tasks by user id.
     *
     * @param userId The id of the user to find tasks for
     * @return List of tasks by user id
     * @throws UserNotFoundException when user not found
     */
    @Override
    public List<TaskResponseDto> findByUserId(Long userId) {
        if (!userService.existsById(userId)) {
            throw new UserNotFoundException("User with id:" + userId + " not found");
        }
        List<Task> tasks = taskRepository.findByUserId(userId);
        return taskMapper.toTaskResponseDtoList(tasks);
    }

    /**
     * Find all tasks by performer id.
     *
     * @param performerId The id of the performer to find tasks for
     * @return List of tasks by performer id
     * @throws PerformerNotFoundException when performer not found
     */
    @Override
    public List<TaskResponseDto> findAllByPerformerId(Long performerId) {
        if (!performerService.existsById(performerId)) {
            throw new PerformerNotFoundException("Performer with id:" + performerId + " not found");
        }
        List<Task> tasks = taskRepository.findAllByPerformerId(performerId);
        return taskMapper.toTaskResponseDtoList(tasks);
    }

    /**
     * Find tasks by task status.
     *
     * @param status The status of the tasks to find
     * @return List of tasks by status
     */
    @Override
    @Transactional
    public List<TaskResponseDto> findByStatus(TaskStatus status) {
        return taskMapper.toTaskResponseDtoList(taskRepository.findTaskByTaskStatus(status));
    }

    /**
     * Add performer to task.
     *
     * @param taskId      The id of the task to add performer to
     * @param performerId The id of the performer to add to the task
     * @return The task with performer
     * @throws PerformerNotFoundException when user same as performer
     * @throws UserNotFoundException      when user is deleted
     * @throws TaskWrongStatusException   when task have status not OPEN
     */
    @Override
    @Transactional
    public TaskResponseDto addPerformer(Long taskId, Long performerId) {
        Task task = taskRepository.findById(taskId).orElseThrow(TaskNotFoundException::new);
        Performer performer = performerService.findByIdReturnPerformer(performerId);

        if (performer.getId().equals(task.getUser().getId()))
            throw new PerformerNotFoundException("Performer can't be same as user");
        if (performer.getUser().isDeleted())
            throw new UserNotFoundException("User is cancel");
        if (!task.getTaskStatus().equals(TaskStatus.OPEN))
            throw new TaskWrongStatusException("Status must be OPEN");
        task.setTaskStatus(TaskStatus.IN_PROGRESS);
        task.setPerformer(performer);
        return taskMapper.toTaskResponseDto(taskRepository.save(task));
    }

    /**
     * Remove performer from task.
     *
     * @param taskId The id of the task to remove performer from
     * @return Task without performer
     * @throws TaskNotFoundException      when task not found
     * @throws PerformerNotFoundException when performer not found
     */
    @Override
    @Transactional
    public TaskResponseDto removePerformer(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Not found task with id: " + taskId));
        if (task.getPerformer() == null)
            throw new PerformerNotFoundException("Performer not found");
        task.setTaskStatus(TaskStatus.OPEN);
        task.setPerformer(null);
        return taskMapper.toTaskResponseDto(taskRepository.save(task));
    }

    /**
     * Update task status by task id.
     *
     * @param taskId The id of the task to update status
     * @param status The new status to update to
     * @return The updated task response
     * @throws TaskNotFoundException    when task not found
     * @throws TaskWrongStatusException when task have status CANCELED or COMPLETED
     */
    @Override
    @Transactional
    public TaskResponseDto updateStatusById(Long taskId, TaskStatus status) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Not found task with id: " + taskId));
        if (task.getTaskStatus().equals(TaskStatus.CANCELED)
                || task.getTaskStatus().equals(TaskStatus.COMPLETED)) {
            throw new TaskWrongStatusException("Task have status: " + task.getTaskStatus());
        }

        // Can't update task status to COMPLETED or IN_PROGRESS when performer is null
        if((status.equals(TaskStatus.COMPLETED) || status.equals(TaskStatus.IN_PROGRESS))
                && task.getPerformer() == null){
            throw new TaskWrongStatusException(String
                    .format("Can't update task status to %s because performer is absent", status));
        }

        // Only task owner can change task status to COMPLETED
        if(!userService.isCurrentUserAdmin()) {
            if (!userService.getCurrentUser().getId().equals(task.getUser().getId())) {
                throw new UserAccessDeniedException("Access denied");
            }
        }

        task.setTaskStatus(status);
        Task updatedTask = taskRepository.save(task);

        // If the task is COMPLETED, then increase the counter of completed tasks for the performer and the user
        if (status.equals(TaskStatus.COMPLETED)) {
            userService.increaseTaskCounterUp(task.getUser());
            performerService.increaseTaskCounterUp(task.getPerformer());
        }

        return taskMapper.toTaskResponseDto(updatedTask);
    }

    /**
     * Get all completed user tasks to which need to send feedbacks
     *
     * @param userId The ID of the user for whom the tasks need to send feedbacks
     * @return List of tasks
     */
    @Override
    public List<TaskResponseDto> findUnrefereedByUserId(Long userId) {
        userService.findById(userId);
        List<Task> tasks = taskRepository.findUnrefereedByUserId(userId);
        return taskMapper.toTaskResponseDtoList(tasks);
    }

    /**
     * Get all completed performer tasks to which need to send feedbacks
     *
     * @param performerId The ID of the performer for whom the tasks need to send feedbacks
     * @return List of tasks
     */
    @Override
    public List<TaskResponseDto> findUnrefereedByPerformerId(Long performerId) {
        performerService.findById(performerId);
        List<Task> tasks = taskRepository.findUnrefereedByPerformerId(performerId);
        return taskMapper.toTaskResponseDtoList(tasks);
    }

    /**
     * Retrieve a list of tasks that are available for a specific performer to work on.
     *
     * @param performerId The ID of the performer for whom the tasks should be available
     * @return A list of TaskResponseDto objects representing the available tasks for the performer
     */
    @Override
    public List<TaskResponseDto> findAvailableForPerformer(Long performerId) {
        Performer performer = performerService.findByIdReturnPerformer(performerId);

        List<Task> tasks = taskRepository.findAllByTaskStatusAndCategoryIn(TaskStatus.OPEN,
                performer.getCategories());
        return taskMapper.toTaskResponseDtoList(tasks);
    }


}
