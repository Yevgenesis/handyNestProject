package codezilla.handynestproject.service.impl;

import codezilla.handynestproject.dto.task.TaskRequestDto;
import codezilla.handynestproject.dto.task.TaskResponseDto;
import codezilla.handynestproject.dto.task.TaskUpdateRequestDto;
import codezilla.handynestproject.exception.CategoryNotFoundException;
import codezilla.handynestproject.exception.TaskNotFoundException;
import codezilla.handynestproject.exception.UserNotFoundException;
import codezilla.handynestproject.mapper.TaskMapper;
import codezilla.handynestproject.model.entity.*;
import codezilla.handynestproject.model.enums.TaskStatus;
import codezilla.handynestproject.repository.*;
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
    public List<TaskResponseDto> getAllTasks() {
        List<Task> tasks = taskRepository.findAll();
        List<TaskResponseDto> dtos = taskMapper.toTaskResponseDtoList(tasks);
        return dtos;
    }

    @Override
    public TaskResponseDto getTaskById(Long taskId) {
        Optional<Task> task = taskRepository.findById(taskId);
        TaskResponseDto dto = taskMapper.toTaskResponseDto(task.get()); // ToDo exception
        return dto;
    }

    @Override
    public List<TaskResponseDto> getAvailableTasks() {
        List<Task> tasks = taskRepository.findAll().stream()
                .filter(t -> t.getTaskStatus().equals(TaskStatus.OPEN))
                .toList();
        List<TaskResponseDto> dtos = taskMapper.toTaskResponseDtoList(tasks);
        return dtos;
    }

    @Override
    public List<TaskResponseDto> getTasksByUserId(Long userId) {
        Optional<Task> task = taskRepository.findByUserId(userId);
        List<TaskResponseDto> dtos = taskMapper.toTaskResponseDtoList(task.stream().toList());
        return dtos;
    }

    @Override
    public List<Task> getTasksByPerformerId(Long performerId) {
        Optional<Performer> performer = performerRepository.findById(performerId);
        List<Task> tasks = taskRepository.findAll();
         if(tasks.get(0).getTaskStatus().equals(TaskStatus.IN_PROGRESS)){
             tasks.stream()
                     .filter(t -> t.getPerformer().equals(performer.get()))
                     .toList();
             return tasks;
         }


         return null;
    }
//TODO написать эксепшн если юзер и перформер совпадают
    @Override
    public Task addPerformerToTask(Long taskId, Long performerId) {
        Task task = taskRepository.findById(taskId).orElseThrow(TaskNotFoundException::new);
        Optional<Performer> performer = performerRepository.findById(performerId);
        if (performer.get().equals(task.getUser())) { // ToDo exception (проверить ID-шки юзера и перформера )
            throw new UserNotFoundException();
        }
        task.setTaskStatus(TaskStatus.IN_PROGRESS);
        task.setPerformer(performer.get());
        return taskRepository.save(task);
    }


    private User getUserFromTaskDto(TaskRequestDto dto) {
        Long userId = dto.userId();
        Optional<User> user = userRepository.findById(userId);
        if (user == null) {
            throw new UserNotFoundException();
        }
        return user.get(); // ToDo check
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
