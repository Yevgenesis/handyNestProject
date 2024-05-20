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
import codezilla.handynestproject.model.entity.Performer;
import codezilla.handynestproject.model.entity.Task;
import codezilla.handynestproject.model.enums.TaskStatus;
import codezilla.handynestproject.repository.CategoryRepository;
import codezilla.handynestproject.repository.PerformerRepository;
import codezilla.handynestproject.repository.TaskRepository;
import codezilla.handynestproject.repository.UserRepository;
import codezilla.handynestproject.repository.WorkingTimeRepository;
import codezilla.handynestproject.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
                .createdOn(Timestamp.valueOf(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)))
                .updatedOn(Timestamp.valueOf(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)))
                .build();

        return taskMapper.toTaskResponseDto(taskRepository.save(task));
    }

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
        task.setTaskStatus(status);
        return taskMapper.toTaskResponseDto(taskRepository.save(task));
    }

}
