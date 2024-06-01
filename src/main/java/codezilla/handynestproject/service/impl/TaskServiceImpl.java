package codezilla.handynestproject.service.impl;

import codezilla.handynestproject.dto.task.TaskRequestDto;
import codezilla.handynestproject.dto.task.TaskResponseDto;
import codezilla.handynestproject.dto.task.TaskUpdateRequestDto;
import codezilla.handynestproject.exception.PerformerNotFoundException;
import codezilla.handynestproject.exception.TaskNotFoundException;
import codezilla.handynestproject.exception.UserNotFoundException;
import codezilla.handynestproject.mapper.AddressMapper;
import codezilla.handynestproject.mapper.TaskMapper;
import codezilla.handynestproject.model.entity.Address;
import codezilla.handynestproject.model.entity.Category;
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

import java.util.*;
import java.util.stream.Collectors;

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


    @Override
    @Transactional
    public TaskResponseDto update(TaskUpdateRequestDto dto) {
        Long workingTimeId = dto.getWorkingTimeId();

        Task task = taskRepository.findById(dto.getId())
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));

        // ToDo исправить. не нужно спрашивать присутствие поля
        // Нужно просто добавлять то, что пришло из DTO
        if (task.getTaskStatus().equals(TaskStatus.OPEN)) {
            Optional.ofNullable(dto.getTitle()).ifPresent(task::setTitle);
            Optional.ofNullable(dto.getDescription()).ifPresent(task::setDescription);
            Optional.ofNullable(dto.getPrice()).ifPresent(task::setPrice);
            Optional.ofNullable(dto.getAddressDto())
                    .ifPresent(addressDto -> task.setAddress(addressMapper.dtoToAddress(addressDto)));
            Optional.ofNullable(workingTimeService.findWorkingTimeById(workingTimeId))
                    .ifPresent(task::setWorkingTime);
        } else {
            throw new TaskNotFoundException("Task have status: " + task.getTaskStatus() +
                    " and can't be updated");
        }

        return taskMapper.toTaskResponseDto(taskRepository.save(task));
    }


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

    @Override
    @Transactional
    public List<TaskResponseDto> findAll() {
        List<Task> tasks = taskRepository.findAll();
        return taskMapper.toTaskResponseDtoList(tasks);
    }

    @Override
    @Transactional
    public TaskResponseDto findById(Long taskId) {
        Optional<Task> task = Optional.of(taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task with id:" + taskId + " not found")));
        return taskMapper.toTaskResponseDto(task.get());
    }

    @Override
    public boolean existsById(Long taskId) {
        return taskRepository.existsById(taskId);
    }



    @Override
    public Task findTaskEntityByIdAndParticipantsId(Long taskId, Long userId) {

        Optional<Task> task = Optional.of(taskRepository
                .findTaskByIdAndStatusIsNotOPENAndPerformerOrUser(taskId, userId)
                .orElseThrow(() -> new TaskNotFoundException("Task with id:" + taskId + " not found")));
        return task.get();
    }

    @Override
    @Transactional
    public List<TaskResponseDto> findAvailableTasks() {
        List<Task> tasks = taskRepository.findTaskByTaskStatus(TaskStatus.OPEN);
        return taskMapper.toTaskResponseDtoList(tasks);
    }

    @Override
    public List<TaskResponseDto> findByUserId(Long userId) {
        if (!userService.existsById(userId)) {
            throw new UserNotFoundException("User with id:" + userId + " not found");
        }
        List<Task> tasks = taskRepository.findByUserId(userId);
        return taskMapper.toTaskResponseDtoList(tasks);
    }

    @Override
    public List<TaskResponseDto> findByPerformerId(Long performerId) {
        if (!performerService.existsById(performerId)) {
            throw new PerformerNotFoundException("Performer with id:" + performerId + " not found");
        }
        List<Task> tasks = taskRepository.findTasksByPerformerId(performerId);
        return taskMapper.toTaskResponseDtoList(tasks);
    }

    @Override
    @Transactional
    public List<TaskResponseDto> findByStatus(TaskStatus status) {
        return taskMapper.toTaskResponseDtoList(taskRepository.findTaskByTaskStatus(status));
    }

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
            throw new TaskNotFoundException("Status must be OPEN");
        task.setTaskStatus(TaskStatus.IN_PROGRESS);
        task.setPerformer(performer);
        return taskMapper.toTaskResponseDto(taskRepository.save(task));
    }

    @Override
    @Transactional
    public TaskResponseDto removePerformer(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task with id:" + taskId + " not found"));
        if (task.getPerformer() == null)
            throw new PerformerNotFoundException("Performer not found");
        task.setTaskStatus(TaskStatus.OPEN);
        task.setPerformer(null);
        return taskMapper.toTaskResponseDto(taskRepository.save(task));
    }

    @Override
    @Transactional
    public TaskResponseDto updateStatusById(Long taskId, TaskStatus status) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task with id:" + taskId + " not found"));
        if (task.getTaskStatus().equals(TaskStatus.CANCELED)
                || task.getTaskStatus().equals(TaskStatus.COMPLETED)) {
            throw new TaskNotFoundException("Task have status: " + task.getTaskStatus());
        }
        // TODO только заказчик может изменить статус на COMPLETED

        task.setTaskStatus(status);
        Task updatedTask = taskRepository.save(task);

        // Если таск COMPLETED, то увеличить счётчики выполненных заданий у перформера и юзера
        if (status.equals(TaskStatus.COMPLETED)) {
            userService.increaseTaskCounterUp(task.getUser());
            performerService.increaseTaskCounterUp(task.getPerformer());
        }

        return taskMapper.toTaskResponseDto(updatedTask);
    }

    // Достать все завершенные таски юзера на которые нужно отправить фитбеки
    @Override
    public List<TaskResponseDto> findUnrefereedByUserId(Long userId) {
        userService.findById(userId);
        List<Task> tasks = taskRepository.findUnrefereedByUserId(userId);
        return taskMapper.toTaskResponseDtoList(tasks);
    }

    // Достать все завершенные таски перформера на которые нужно отправить фитбеки
    @Override
    public List<TaskResponseDto> findUnrefereedByPerformerId(Long performerId) {
        performerService.findById(performerId);
        List<Task> tasks = taskRepository.findUnrefereedByPerformerId(performerId);
        return taskMapper.toTaskResponseDtoList(tasks);
    }

    // Достать все таски, которые совпадают по категориям конкретного перформера
    @Override
    public List<TaskResponseDto> findAvailableForPerformer(Long performerId) {
        Performer performer = performerService.findByIdReturnPerformer(performerId);

        List<Task> tasks = taskRepository.findAllByTaskStatusAndCategoryIn(TaskStatus.OPEN, performer.getCategories());
        return taskMapper.toTaskResponseDtoList(tasks);
    }


}
