package codezilla.handynestproject.service.impl;

import codezilla.handynestproject.dto.task.TaskRequestDto;
import codezilla.handynestproject.exception.CategoryNotFoundException;
import codezilla.handynestproject.exception.UserNotFoundException;
import codezilla.handynestproject.exception.WorkingTimeNotFoundException;
import codezilla.handynestproject.model.entity.Category;
import codezilla.handynestproject.model.entity.Task;
import codezilla.handynestproject.model.entity.User;
import codezilla.handynestproject.model.entity.WorkingTime;
import codezilla.handynestproject.model.entity.enums.TaskStatus;
import codezilla.handynestproject.repository.CategoryRepository;
import codezilla.handynestproject.repository.TaskRepository;
import codezilla.handynestproject.repository.UserRepository;
import codezilla.handynestproject.repository.WorkingTimeRepository;
import codezilla.handynestproject.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
                .address(dto.address())
                .taskStatus(TaskStatus.OPEN)
                .workingTime(getWorkingTimeFromDto(dto))
                .category(getCategoryFromDto(dto))
                .user(getUserFromTaskDto(dto))
                .build();

        return taskRepository.save(task);
    }

    @Override
    public Task updateTask(TaskRequestDto dto) {

        return null;
    }

    @Override
    public void deleteTaskById(Long taskId) {

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
        WorkingTime workingTime = workingTimeRepository.getById(workingTimeId)
        if (workingTime == null) {
            throw new WorkingTimeNotFoundException();
        }
        return workingTime;
    }

    private Category getCategoryFromDto(TaskRequestDto dto) {
        Long categoryId = dto.categoryId();
        Category category = categoryRepository.getById(categoryId);
        if (category == null) {
            throw new CategoryNotFoundException();
        }
        return category;
    }
}
