package codezilla.handynestproject.service.impl;

import codezilla.handynestproject.dto.task.TaskRequestDto;
import codezilla.handynestproject.dto.task.TaskUpdateRequestDto;
import codezilla.handynestproject.exception.CategoryNotFoundException;
import codezilla.handynestproject.exception.TaskNotFoundException;
import codezilla.handynestproject.exception.UserNotFoundException;
import codezilla.handynestproject.exception.WorkingTimeNotFoundException;
import codezilla.handynestproject.model.entity.*;
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

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final WorkingTimeRepository workingTimeRepository;
    private final CategoryRepository categoryRepository;
    private final PerformerRepository performerRepository;


    @Override
    public Task createTask(TaskRequestDto dto) {
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

        return taskRepository.save(task);
    }
    @Override
    public Task createTaskByUserId(Long userId, TaskRequestDto dto) {
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
        return taskRepository.save(task);
    }

    @Override
    public Task updateTask(TaskUpdateRequestDto dto) {
        Task task = taskRepository.findById(dto.getId()).orElseThrow(TaskNotFoundException::new);
        if (dto.getWorkingTimeId() != null) {
            task.setWorkingTime(getWorkingTimeFromWorkingTimeId(dto.getWorkingTimeId()));
        }
        if (dto.getTitle() != null) {
            task.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            task.setDescription(dto.getDescription());
        }
        if (dto.getPrice() != null) {
            task.setPrice(dto.getPrice());
        }
        if (dto.getAddress() != null) {
            task.setAddress(dto.getAddress());
        }

        if (dto.getCategory() != null) {
            task.setCategory(dto.getCategory());
        }


        return taskRepository.save(task);
    }

    @Override
    public void deleteTaskById(Long taskId) {
        Task task = taskRepository.findById(taskId).orElseThrow(TaskNotFoundException::new);
        taskRepository.delete(task);
    }

    @Override
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    @Override
    public Task getTaskById(Long taskId) {
        return taskRepository.findTaskById(taskId);
    }

    @Override
    public List<Task> getAvailableTasks() {
        List<Task> tasks = taskRepository.findAll();
        return tasks.stream()
                .filter(t -> t.getTaskStatus().equals(TaskStatus.OPEN))
                .toList();
    }

    @Override
    public List<Task> getTasksByUserId(Long userId) {
        User user = userRepository.findUserById(userId);
        List<Task> tasks = taskRepository.findAll();
        return tasks.stream()
                .filter(t->t.getUser().equals(user))
                .toList();
    }

    @Override
    public List<Task> getTasksByPerformerId(Long performerId) {
        Performer performer = performerRepository.findPerformerById(performerId);
        List<Task> tasks = taskRepository.findAll();
        return tasks.stream()
                .filter(t->t.getPerformer().equals(performer))
                .toList();
    }
//TODO написать эксепшн если юзер и перформер совпадают
    @Override
    public Task addPerformerToTask(Long taskId, Long performerId) {
        Task task = taskRepository.findById(taskId).orElseThrow(TaskNotFoundException::new);
        Performer performer = performerRepository.findPerformerById(performerId);
        if(performer.getUser().equals(task.getUser())){
            throw new UserNotFoundException();
        }
        task.setTaskStatus(TaskStatus.IN_PROGRESS);
        task.setPerformer(performer);
        return taskRepository.save(task);
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
        WorkingTime workingTime = workingTimeRepository.getById(workingTimeId);
        if (workingTime == null) {
            throw new WorkingTimeNotFoundException();
        }
        return workingTime;
    }

    private Category getCategoryFromDto(TaskRequestDto dto) {
        Long categoryId = dto.categoryId();
        return categoryRepository.findById(categoryId).orElseThrow(CategoryNotFoundException::new);
    }

    private WorkingTime getWorkingTimeFromWorkingTimeId(Long workingTimeId) {
        return workingTimeRepository.getById(workingTimeId);
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
