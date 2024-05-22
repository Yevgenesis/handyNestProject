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
    @Transactional
    public TaskResponseDto create(TaskRequestDto dto) {
        Task task = Task.builder()
                .title(dto.title())
                .description(dto.description())
                .price(dto.price())
                .address(Address.builder()
                        .street(dto.street())
                        .city(dto.city())
                        .zip(dto.zip())
                        .country(dto.country())
                        .build())
                .taskStatus(TaskStatus.OPEN)
                .isPublish(dto.isPublish())
                .workingTime(workingTimeRepository.findWorkingTimeById(dto.workingTimeId()))
                .category(categoryRepository.findById(dto.categoryId()).get())
                .user(userRepository.findById(dto.userId()).get())

                .build();

        return taskMapper.toTaskResponseDto(taskRepository.save(task));
    }


    // ToDo Исправить,
    @Override
    @Transactional
    public TaskResponseDto update(TaskUpdateRequestDto dto) {
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
    public void cancelById(Long taskId) {
        Task task = taskRepository.findById(taskId).orElseThrow(TaskNotFoundException::new);
        task.setTaskStatus(TaskStatus.CANCELED);
    }

    @Override
    @Transactional
    public List<TaskResponseDto> getAll() {
        List<Task> tasks = taskRepository.findAll();
        return taskMapper.toTaskResponseDtoList(tasks);
    }



    @Override
    @Transactional
    public TaskResponseDto getById(Long taskId) {
        Optional<Task> task = Optional.of(taskRepository.findById(taskId)
                .orElseThrow(TaskNotFoundException::new));
        return  taskMapper.toTaskResponseDto(task.get());
    }

    @Override
    public Task getTaskEntityByIdAndParticipantsId(Long taskId, Long userId) {
        Optional<Task> task = Optional.of(taskRepository
                .findTaskByIdAndStatusIsNotOPENAndPerformerOrUser(taskId, userId)
                .orElseThrow(TaskNotFoundException::new));
        return task.get();
    }
    @Override
    @Transactional
    public List<TaskResponseDto> getAvailableTasks() {
        List<Task> tasks = taskRepository.findTaskByTaskStatus(TaskStatus.OPEN);
        return taskMapper.toTaskResponseDtoList(tasks);
    }

    @Override
    public List<TaskResponseDto> getByUserId(Long userId) {
        List<Task> tasks = taskRepository.findByUserId(userId);
        return taskMapper.toTaskResponseDtoList(tasks);
    }

    @Override
    public List<TaskResponseDto> getByPerformerId(Long performerId) {

       List<Task> tasks = taskRepository.findTasksByPerformerId(performerId);
        return taskMapper.toTaskResponseDtoList(tasks);


    }

    @Override
    @Transactional
    public List<TaskResponseDto> getByStatus(TaskStatus status) {
        return taskMapper.toTaskResponseDtoList(taskRepository.findTaskByTaskStatus(status));
    }
//TODO проверить корректность написания эксепшенов
    @Override
    @Transactional
    public TaskResponseDto addPerformer(Long taskId, Long performerId) {
        Task task = taskRepository.findById(taskId).orElseThrow(TaskNotFoundException::new);
        Performer performer = performerRepository.findById(performerId)
                .orElseThrow(()->new PerformerNotFoundException("Performer can't be same as user"));
        if ( performer.getId().equals(task.getUser().getId()))
            throw new PerformerNotFoundException("Performer can't be same as user");
        if(performer.getUser().isDeleted())
            throw new UserNotFoundException("User is cancel");
         if(!task.getTaskStatus().equals(TaskStatus.OPEN))
            throw new TaskNotFoundException("Status must be OPEN");
        task.setTaskStatus(TaskStatus.IN_PROGRESS);
        task.setPerformer(performer);
        return taskMapper.toTaskResponseDto(taskRepository.save(task));
    }
    @Override
    @Transactional
    public TaskResponseDto removePerformer(Long taskId) {
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


}
