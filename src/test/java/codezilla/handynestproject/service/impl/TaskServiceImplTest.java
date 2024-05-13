package codezilla.handynestproject.service.impl;

import codezilla.handynestproject.dto.task.TaskRequestDto;
import codezilla.handynestproject.dto.task.TaskResponseDto;
import codezilla.handynestproject.dto.task.TaskUpdateRequestDto;
import codezilla.handynestproject.mapper.CategoryMapper;
import codezilla.handynestproject.mapper.CategoryMapperImpl;
import codezilla.handynestproject.mapper.PerformerMapper;
import codezilla.handynestproject.mapper.PerformerMapperImpl;
import codezilla.handynestproject.mapper.TaskMapper;
import codezilla.handynestproject.mapper.TaskMapperImpl;
import codezilla.handynestproject.model.entity.Address;
import codezilla.handynestproject.model.entity.Category;
import codezilla.handynestproject.model.entity.Performer;
import codezilla.handynestproject.model.entity.Task;
import codezilla.handynestproject.model.entity.WorkingTime;
import codezilla.handynestproject.model.enums.TaskStatus;
import codezilla.handynestproject.repository.CategoryRepository;
import codezilla.handynestproject.repository.PerformerRepository;
import codezilla.handynestproject.repository.TaskRepository;
import codezilla.handynestproject.repository.UserRepository;
import codezilla.handynestproject.repository.WorkingTimeRepository;
import codezilla.handynestproject.service.CategoryService;
import codezilla.handynestproject.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static codezilla.handynestproject.service.TestData.TASK_REQUEST_DTO;
import static codezilla.handynestproject.service.TestData.TASK_RESPONSE_DTO;
import static codezilla.handynestproject.service.TestData.TEST_CATEGORY;
import static codezilla.handynestproject.service.TestData.TEST_PERFORMER;
import static codezilla.handynestproject.service.TestData.TEST_TASK;
import static codezilla.handynestproject.service.TestData.TEST_USER;
import static codezilla.handynestproject.service.TestData.TEST_WORKING_TIME;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository mockTaskRepository;
    @Mock
    private UserRepository mockUserRepository;
    @Mock
    private WorkingTimeRepository mockWorkingTimeRepository;
    @Mock
    private CategoryRepository mockCategoryRepository;
    @Mock
    private PerformerRepository mockPerformerRepository;

    private TaskMapper taskMapper = new TaskMapperImpl();
    private CategoryService categoryService;



    private TaskService taskService;


    @BeforeEach
    void setUp() {
        openMocks(this);
        taskService = new TaskServiceImpl(mockUserRepository, mockTaskRepository,
                mockWorkingTimeRepository, mockCategoryRepository, mockPerformerRepository, taskMapper,
                categoryService);

    }

    @DisplayName("createTask then call taskRepository.save()")
    @Test
    void createTaskTest() {
        TaskRequestDto request = TASK_REQUEST_DTO;
        when(mockTaskRepository.save(any())).thenReturn(TEST_TASK);
        when(mockWorkingTimeRepository.findById(any()))
                .thenReturn(Optional.ofNullable(TEST_WORKING_TIME));
        when(mockCategoryRepository.findById(any()))
                .thenReturn(Optional.ofNullable(TEST_CATEGORY));
        when(mockPerformerRepository.findById(any()))
                .thenReturn(Optional.ofNullable(TEST_PERFORMER));
        when(mockUserRepository.findById(any()))
                .thenReturn(Optional.ofNullable(TEST_USER));
        TaskResponseDto actual = taskService.createTask(request);
        assertEquals(TASK_RESPONSE_DTO,actual);




    }

    @DisplayName("updateTask then call once taskRepository.save()")
    @Test
    void updateTaskTest() {
        TaskUpdateRequestDto dto = new TaskUpdateRequestDto(
                1L, "Test Title", "Test Description", 20.0, new Address(), 2L);
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Title");
        task.setDescription("Test Description");
        task.setAddress(new Address());
        task.setWorkingTime(new WorkingTime());
        task.setCategory(new Category());
        task.setPerformer(new Performer());
        task.setTaskStatus(TaskStatus.OPEN);


        when(mockTaskRepository.findById(dto.getId())).thenReturn(Optional.of(task));
        when(mockTaskRepository.save(task)).thenReturn(task);
        taskService.updateTask(dto);
        Mockito.verify(mockTaskRepository, Mockito.times(1)).save(task);

        assertEquals(dto.getId(), task.getId());
        assertEquals(dto.getTitle(), task.getTitle());
        assertEquals(dto.getDescription(), task.getDescription());


    }

    @DisplayName("deleteTaskById then call once taskRepository.delete")
    @Test
    void deleteTaskByIdTest() {
        Long taskId = 1L;
        Task task = new Task();
        task.setId(taskId);
        Mockito.doReturn(Optional.of(task)).when(mockTaskRepository).findById(taskId);
        when(mockTaskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        taskService.deleteTaskById(task.getId());
        Mockito.verify(mockTaskRepository, Mockito.times(1)).delete(task);
    }

    @DisplayName("getTaskById then call once taskRepository.findById()")
    @Test
    void getTaskByIdTest() {

        Long taskId = 1L;
        Task task = new Task();
        task.setId(taskId);
        Mockito.doReturn(Optional.of(task)).when(mockTaskRepository).findById(taskId);
        taskService.getTaskById(taskId);
        Mockito.verify(mockTaskRepository, Mockito.times(1)).findById(taskId);
        assertEquals(taskId, task.getId());
    }

    @DisplayName("getAllTasks then call once taskRepository.findAll")
    @Test
    void getAllTasksTest() {
        taskService.getAllTasks();
        Mockito.verify(mockTaskRepository, Mockito.times(1)).findAll();
    }

    @DisplayName("getTasksByStatus then call once taskRepository.findTaskByTaskStatus()")
    @Test
    void getTasksByStatusTest() {
        taskService.getTasksByStatus(TaskStatus.OPEN);
        Mockito.verify(mockTaskRepository, Mockito.times(1))
                .findTaskByTaskStatus(TaskStatus.OPEN);


    }

    @Test
    void getAvailableTasksTest() {

    }

    @Test
    void getTasksByUserIdTest() {

    }

    @Test
    void getTasksByPerformerId() {
    }

//    @Test
//    void addPerformerToTask() {
//        Task task = new Task();
//        User user = new User();
//        Performer performer = new Performer();
//        performer.setId(2L);
//        task.setUser(user);
//        user.setId(1L);
//        task.setPerformer(performer);
//        task.setId(1L);
//
//        Mockito.doReturn(Optional.of(task)).when(mockTaskRepository).findById(task.getId());
//        Mockito.doReturn(Optional.of(performer)).when(mockPerformerRepository).findById(performer.getId());
//
//
//
//
//        when(this.mockPerformerRepository.findById(2L)).thenReturn(Optional.of(performer));
//        when(mockTaskRepository.findById(1L)).thenReturn(Optional.of(task));
//        when(mockUserRepository.findById(1L)).thenReturn(Optional.of(user));
//
//        TaskResponseDto responseDto = taskService.addPerformerToTask(1L, 2L);
//
//        assertNotNull(responseDto);
//        assertEquals(TaskStatus.IN_PROGRESS, task.getTaskStatus());
//        assertEquals(performer, task.getPerformer());
//
//    }

    @Test
    void removePerformerFromTask() {
    }

    @Test
    void updateTaskStatusById() {
    }

    @Test
    void createTaskByUserId() {
    }
}