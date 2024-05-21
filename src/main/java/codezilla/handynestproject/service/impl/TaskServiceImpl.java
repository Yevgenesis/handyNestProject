package codezilla.handynestproject.service.impl;

import codezilla.handynestproject.dto.task.TaskRequestDto;
import codezilla.handynestproject.dto.task.TaskResponseDto;
import codezilla.handynestproject.dto.task.TaskUpdateRequestDto;
import codezilla.handynestproject.exception.*;
import codezilla.handynestproject.mapper.AddressMapper;
import codezilla.handynestproject.mapper.TaskMapper;
import codezilla.handynestproject.model.entity.*;
import codezilla.handynestproject.model.enums.TaskStatus;
import codezilla.handynestproject.repository.*;
import codezilla.handynestproject.service.PerformerService;
import codezilla.handynestproject.service.TaskService;
import codezilla.handynestproject.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final WorkingTimeRepository workingTimeRepository;
    private final CategoryRepository categoryRepository;
    private final PerformerRepository performerRepository;
    private final TaskMapper taskMapper;
    private final AddressMapper addressMapper;
    private final UserService userService;
    private final PerformerService performerService;


    @Override
    public TaskResponseDto createTask(TaskRequestDto dto) {
        Task task = Task.builder()
                .title(dto.title())
                .description(dto.description())
                .price(dto.price())
                .address(getAddressFromDto(dto))
                .taskStatus(TaskStatus.OPEN)
                .isPublish(dto.isPublish())
                .workingTime(getWorkingTimeFromDto(dto))
                .category(getCategoryFromDto(dto))
                .user(getUserFromTaskDto(dto))
                .build();

        return taskMapper.toTaskResponseDto(taskRepository.save(task));
    }
    @Override
    public TaskResponseDto createTaskByUserId(Long userId, TaskRequestDto dto) {
        Optional<User> user = userRepository.findById(userId);
        Task task = Task.builder()
                .title(dto.title())
                .description(dto.description())
                .price(dto.price())
                .address(getAddressFromDto(dto))
                .taskStatus(TaskStatus.OPEN)
                .isPublish(dto.isPublish())
                .workingTime(getWorkingTimeFromDto(dto))
                .category(getCategoryFromDto(dto))
                .user(user.get()) // ToDo check
                .build();
        return taskMapper.toTaskResponseDto(taskRepository.save(task));
    }


    // ToDo Исправить,
    @Override
    public TaskResponseDto updateTask(TaskUpdateRequestDto dto) {

        Long workingTimeId = dto.getWorkingTimeId();

        Task task = taskRepository.findById(dto.getId())
                .orElseThrow(()->new TaskNotFoundException("Task not found"));

        if(task.getTaskStatus().equals(TaskStatus.OPEN)) {
            Optional.ofNullable(dto.getTitle()).ifPresent(task::setTitle);
            Optional.ofNullable(dto.getDescription()).ifPresent(task::setDescription);
            Optional.ofNullable(dto.getPrice()).ifPresent(task::setPrice);
            Optional.ofNullable(dto.getAddressDto())
                    .ifPresent(addressDto -> task.setAddress(addressMapper.dtoToAddress(addressDto)));
            Optional.ofNullable(workingTimeRepository.findWorkingTimeById(workingTimeId))
                    .ifPresent(task::setWorkingTime);
        }
        else {
            throw new TaskNotFoundException("Task have status: " + task.getTaskStatus()+
                    " and can't be updated");
        }


        return  taskMapper.toTaskResponseDto(taskRepository.save(task));
    }


    @Override
    public void deleteTaskById(Long taskId) {
        Task task = taskRepository.findById(taskId).orElseThrow(TaskNotFoundException::new);
        taskRepository.delete(task);
    }

    @Override
    public List<TaskResponseDto> getAllTasks() {
        return taskMapper.toTaskResponseDtoList(taskRepository.findAll());
    }



    @Override
    public TaskResponseDto getTaskById(Long taskId) {
        Optional<Task> task = Optional.of(taskRepository.findById(taskId)
                .orElseThrow(TaskNotFoundException::new));
        return  taskMapper.toTaskResponseDto(task.get());
    }

    @Override
    public Task getTaskEntityByIdAndParticipantsId(Long taskId, Long userId) {
        Optional<Task> task = Optional.of(taskRepository.findTaskByIdAndStatusIsNotOPENAndPerformerOrUser(taskId, userId)
                .orElseThrow(TaskNotFoundException::new));
        return task.get();
    }
    @Override
    public List<TaskResponseDto> getAvailableTasks() {
        List<Task> tasks = taskRepository.findTaskByTaskStatus(TaskStatus.OPEN);
        return taskMapper.toTaskResponseDtoList(tasks);
    }

    @Override
    public List<TaskResponseDto> getTasksByUserId(Long userId) {
        Optional<Task> task = Optional.ofNullable(taskRepository.findByUserId(userId).
                orElseThrow(TaskNotFoundException::new));
        return taskMapper.toTaskResponseDtoList(task.stream().toList());
    }

    @Override
    public List<TaskResponseDto> getTasksByPerformerId(Long performerId) {

       List<Task> tasks = taskRepository.findTasksByPerformerId(performerId);
        return taskMapper.toTaskResponseDtoList(tasks);


    }

    @Override
    public List<TaskResponseDto> getTasksByStatus(TaskStatus status) {
        return taskMapper.toTaskResponseDtoList(taskRepository.findTaskByTaskStatus(status));
    }
//TODO проверить корректность написания эксепшенов
    @Override
    public TaskResponseDto addPerformerToTask(Long taskId, Long performerId) {
        Task task = taskRepository.findById(taskId).orElseThrow(TaskNotFoundException::new);
        Performer performer = performerRepository.findById(performerId)
                .orElseThrow(()->new PerformerNotFoundException("Performer can't be same as user"));
        if ( performer.getId().equals(task.getUser().getId()))
            throw new PerformerNotFoundException("Performer can't be same as user");
        if(performer.getUser().isDeleted())
            throw new UserNotFoundException("User is delete");
         if(!task.getTaskStatus().equals(TaskStatus.OPEN))
            throw new TaskNotFoundException("Status must be OPEN");
        task.setTaskStatus(TaskStatus.IN_PROGRESS);
        task.setPerformer(performer);
        return taskMapper.toTaskResponseDto(taskRepository.save(task));
    }
    @Override
    public TaskResponseDto removePerformerFromTask(Long taskId) {
        Task task = taskRepository.findById(taskId).orElseThrow(TaskNotFoundException::new);
        if(task.getPerformer() == null)
            throw new PerformerNotFoundException("Performer not found");
        task.setTaskStatus(TaskStatus.OPEN);
        task.setPerformer(null);
        return taskMapper.toTaskResponseDto(taskRepository.save(task));
    }

    @Override
    @Transactional
    public TaskResponseDto updateTaskStatusById(Long taskId, TaskStatus status){
        Task task = taskRepository.findById(taskId).orElseThrow(TaskNotFoundException::new);
                if(task.getTaskStatus().equals(TaskStatus.CANCELED)
                        || task.getTaskStatus().equals(TaskStatus.COMPLETED)){
                    throw new TaskNotFoundException("Task have status: " + task.getTaskStatus());
                }
        // только заказчик может изменить статус на COMPLETED
        task.setTaskStatus(status);
        Task updatedTask = taskRepository.save(task);

        // Если таск COMPLETED, то увеличить счётчики выполненных заданий у перформера и юзера
        if (status.equals(TaskStatus.COMPLETED)) {
            userService.increaseTaskCounterUp(task.getUser());
            performerService.increaseTaskCounterUp(task.getPerformer());
        }

        return taskMapper.toTaskResponseDto(updatedTask);
    }


    private User getUserFromTaskDto(TaskRequestDto dto) {
       return userRepository.findById(dto.userId())
               .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    private WorkingTime getWorkingTimeFromDto(TaskRequestDto dto) {

        return workingTimeRepository.findById(dto.workingTimeId())
                .orElseThrow(WorkingTimeNotFoundException::new)
                ;
    }

    private Category getCategoryFromDto(TaskRequestDto dto) {
        Long categoryId = dto.categoryId();
        return categoryRepository.findById(categoryId)
                .orElseThrow(CategoryNotFoundException::new);
    }

    private Address getAddressFromDto(TaskRequestDto dto) {
        return Address.builder()
                .country(dto.country())
                .city(dto.city())
                .street(dto.street())
                .zip(dto.zip())
                .build();
    }
}
