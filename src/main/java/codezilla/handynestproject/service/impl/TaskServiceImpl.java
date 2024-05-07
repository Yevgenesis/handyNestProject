package codezilla.handynestproject.service.impl;

import codezilla.handynestproject.dto.task.TaskRequestDto;
import codezilla.handynestproject.dto.task.TaskResponseDto;
import codezilla.handynestproject.dto.task.TaskUpdateRequestDto;
import codezilla.handynestproject.exception.CategoryNotFoundException;
import codezilla.handynestproject.exception.TaskNotFoundException;
import codezilla.handynestproject.exception.UserNotFoundException;
import codezilla.handynestproject.exception.WorkingTimeNotFoundException;
import codezilla.handynestproject.mapper.TaskMapper;
import codezilla.handynestproject.model.entity.Address;
import codezilla.handynestproject.model.entity.Category;
import codezilla.handynestproject.model.entity.Performer;
import codezilla.handynestproject.model.entity.Task;
import codezilla.handynestproject.model.entity.User;
import codezilla.handynestproject.model.entity.WorkingTime;
import codezilla.handynestproject.model.enums.TaskStatus;
import codezilla.handynestproject.repository.CategoryRepository;
import codezilla.handynestproject.repository.PerformerRepository;
import codezilla.handynestproject.repository.TaskRepository;
import codezilla.handynestproject.repository.UserRepository;
import codezilla.handynestproject.repository.WorkingTimeRepository;
import codezilla.handynestproject.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
        User user = userRepository.findUserById(userId);
        Task task = Task.builder()
                .title(dto.title())
                .description(dto.description())
                .price(dto.price())
                .address(getAddressFromDto(dto))
                .taskStatus(TaskStatus.OPEN)
                .isPublish(dto.isPublish())
                .workingTime(getWorkingTimeFromDto(dto))
                .category(getCategoryFromDto(dto))
                .user(user)
                .build();
        return taskMapper.toTaskResponseDto(taskRepository.save(task));
    }

    @Override
    public TaskResponseDto updateTask(TaskUpdateRequestDto dto) {
        Task task = taskRepository.findById(dto.getId()).orElseThrow(TaskNotFoundException::new);

            task.setWorkingTime(workingTimeRepository.findById(dto.getWorkingTimeId())
                    .orElseThrow(WorkingTimeNotFoundException::new));
            task.setTitle(dto.getTitle());
            task.setDescription(dto.getDescription());
            task.setPrice(dto.getPrice());
            task.setAddress(dto.getAddress());
            task.setCategory(categoryRepository.findById(dto.getCategory().getId())
                    .orElseThrow(CategoryNotFoundException::new));
        return taskMapper.toTaskResponseDto(taskRepository.save(task));
    }

    @Override
    public void deleteTaskById(Long taskId) {
        Task task = taskRepository.findById(taskId).orElseThrow(TaskNotFoundException::new);
        taskRepository.delete(task);
    }

    @Override
    public List<TaskResponseDto> getAllTasks() {
        List<Task> tasks = taskRepository.findAll();
        return taskMapper.toTaskResponseDtoList(tasks);
    }

    @Override
    public TaskResponseDto getTaskById(Long taskId) {
        Optional<Task> task = Optional.of(taskRepository.findById(taskId)
                .orElseThrow(TaskNotFoundException::new));
        return  taskMapper.toTaskResponseDto(task.get());
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
        Optional<Performer> performer = performerRepository.findById(performerId);
        List<Task> tasks = taskRepository.findAll();
         if(tasks.get(0).getTaskStatus().equals(TaskStatus.IN_PROGRESS)){
             tasks.stream()
                     .filter(t->t.getPerformer().equals(performer))
                     .toList();
             return taskMapper.toTaskResponseDtoList(tasks);
         }


         return null;
    }
//TODO написать эксепшн если юзер и перформер совпадают
    @Override
    public TaskResponseDto addPerformerToTask(Long taskId, Long performerId) {
        Task task = taskRepository.findById(taskId).orElseThrow(TaskNotFoundException::new);
        Optional<Performer> performer = performerRepository.findById(performerId);
        if (performer.get().getId().equals(task.getUser().getId())
        || !userRepository.findUserById(performerId).isDeleted()
        || !task.getTaskStatus().equals(TaskStatus.OPEN)) { // ToDo exception
            throw new IllegalArgumentException();
        }
        task.setTaskStatus(TaskStatus.IN_PROGRESS);
        task.setPerformer(performer.get());
        return taskMapper.toTaskResponseDto(taskRepository.save(task));
    }


    private User getUserFromTaskDto(TaskRequestDto dto) {
        Long userId = dto.userId();
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new UserNotFoundException();
        }
        return user;
    }

    private WorkingTime getWorkingTimeFromDto(TaskRequestDto dto) {
        Long workingTimeId = dto.workingTimeId();
        Optional<WorkingTime> workingTime = workingTimeRepository.findById(workingTimeId); // ToDo exception
        return workingTime.get();
    }

    private Category getCategoryFromDto(TaskRequestDto dto) {
        Long categoryId = dto.categoryId();
        return categoryRepository.findById(categoryId).orElseThrow(CategoryNotFoundException::new);
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
