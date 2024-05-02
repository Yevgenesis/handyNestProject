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
        if (dto.getTaskStatus() != null) {
            task.setTaskStatus(dto.getTaskStatus());
        }
        if (dto.getCategory() != null) {
            task.setCategory(dto.getCategory());
        }
        if (dto.getUser() != null) {
            task.setUser(dto.getUser());
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
    public List<Task> getAllPublishTasks() {
        List<Task> tasks = taskRepository.findAll();
        return tasks.stream()
                .filter(Task::isPublish).toList();
    }
//
//    @Override
//    public Task addPerformer(Long taskId, Performer performer) {
//        Task task = taskRepository.findById(taskId).orElseThrow(TaskNotFoundException::new);
//
//
//        return null;
//    }

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
