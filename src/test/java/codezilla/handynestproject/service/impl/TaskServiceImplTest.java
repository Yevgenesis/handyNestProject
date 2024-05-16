package codezilla.handynestproject.service.impl;

import codezilla.handynestproject.dto.task.TaskRequestDto;
import codezilla.handynestproject.dto.task.TaskResponseDto;
import codezilla.handynestproject.dto.task.TaskUpdateRequestDto;
import codezilla.handynestproject.mapper.AddressMapper;
import codezilla.handynestproject.mapper.CategoryMapper;
import codezilla.handynestproject.mapper.PerformerMapper;
import codezilla.handynestproject.mapper.TaskMapper;
import codezilla.handynestproject.model.entity.Performer;
import codezilla.handynestproject.model.entity.Task;
import codezilla.handynestproject.model.enums.TaskStatus;
import codezilla.handynestproject.repository.CategoryRepository;
import codezilla.handynestproject.repository.PerformerRepository;
import codezilla.handynestproject.repository.TaskRepository;
import codezilla.handynestproject.repository.UserRepository;
import codezilla.handynestproject.repository.WorkingTimeRepository;
import codezilla.handynestproject.service.CategoryService;
import codezilla.handynestproject.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.Optional;

import static codezilla.handynestproject.TestData.ADDRESS_DTO;
import static codezilla.handynestproject.TestData.TASK_REQUEST_DTO;
import static codezilla.handynestproject.TestData.TASK_RESPONSE_DTO;
import static codezilla.handynestproject.TestData.TASK_RESPONSE_DTO_WITH_PERFORMER;
import static codezilla.handynestproject.TestData.TEST_CATEGORY;
import static codezilla.handynestproject.TestData.TEST_PERFORMER2;
import static codezilla.handynestproject.TestData.TEST_TASK2_IN_PROGRESS;
import static codezilla.handynestproject.TestData.TEST_TASK_OPEN;
import static codezilla.handynestproject.TestData.TEST_USER;
import static codezilla.handynestproject.TestData.TEST_WORKING_TIME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;


@SpringBootTest
class TaskServiceImplTest {


    @MockBean
    private TaskRepository mockTaskRepository;
    @MockBean
    private UserRepository mockUserRepository;
    @MockBean
    private WorkingTimeRepository mockWorkingTimeRepository;
    @MockBean
    private CategoryRepository mockCategoryRepository;
    @MockBean
    private PerformerRepository mockPerformerRepository;

    @Autowired
    private TaskMapper mockTaskMapper;

    @Autowired
    private PerformerMapper mockPerformerMapper;

    @Autowired
    private AddressMapper mockAddressMapper;

    @Autowired
    private CategoryMapper mockCategoryMapper;

    @Autowired
    private CategoryService categoryService;
    @Autowired
    private TaskService taskService;

    @BeforeEach
    void setUp() {
        openMocks(this);
        taskService = new TaskServiceImpl(mockUserRepository, mockTaskRepository,
                mockWorkingTimeRepository, mockCategoryRepository, mockPerformerRepository, mockTaskMapper,
                mockAddressMapper);
        when(mockTaskRepository.save(any())).thenReturn(TEST_TASK_OPEN);
        when(mockTaskRepository.findById(any()))
                .thenReturn(Optional.ofNullable(TEST_TASK_OPEN));
        when(mockWorkingTimeRepository.findById(any()))
                .thenReturn(Optional.ofNullable(TEST_WORKING_TIME));
        when(mockCategoryRepository.findById(any()))
                .thenReturn(Optional.ofNullable(TEST_CATEGORY));

        when(mockUserRepository.findById(any()))
                .thenReturn(Optional.ofNullable(TEST_USER));

    }


    @Test
    void createTaskTest() {

        TaskResponseDto actual = taskService.createTask(TASK_REQUEST_DTO);
        assertEquals(TASK_RESPONSE_DTO, actual);
    }


    @Test
    void updateTaskTest() {
        TaskUpdateRequestDto dto = new TaskUpdateRequestDto(
                1L,
                "Test Title",
                "Test Description",
                0.0,
                ADDRESS_DTO,
                2L);

        TaskResponseDto actual = taskService.updateTask(dto);
        assertEquals(TASK_RESPONSE_DTO, actual);
    }


    //TODO проверить написание теста

    @Test
    void deleteTaskByIdTest() {

        Long taskId = TEST_TASK_OPEN.getId();
        doNothing().when(mockTaskRepository).deleteById(taskId);

    }

    @Test
    void getTaskByIdTest() {
        Task task = TEST_TASK_OPEN.toBuilder().build();
        when(mockTaskRepository.findById(task.getId()))
                .thenReturn(Optional.of(task));
        TaskResponseDto actual = taskService.getTaskById(task.getId());
        assertEquals(TASK_RESPONSE_DTO, actual);
    }


    @Test
    void getAllTasksTest() {

        when(mockTaskRepository.findAll()).thenReturn(List.of(TEST_TASK_OPEN));
        List<TaskResponseDto> tasks = taskService.getAllTasks();
        assertEquals(1, tasks.size());
        verify(mockTaskRepository, Mockito.times(1)).findAll();
    }


    @Test
    void getTasksByStatusTest() {

        when(mockTaskRepository.findTaskByTaskStatus(TaskStatus.IN_PROGRESS))
                .thenReturn(List.of(TEST_TASK2_IN_PROGRESS));
        List<TaskResponseDto> actual = taskService.getTasksByStatus(TaskStatus.IN_PROGRESS);
        assertEquals(1, actual.size());
        verify(mockTaskRepository, Mockito.times(1))
                .findTaskByTaskStatus(TaskStatus.IN_PROGRESS);

    }

    @Test
    void getAvailableTasksTest() {
        when(mockTaskRepository.findTaskByTaskStatus(TaskStatus.OPEN)).thenReturn(List.of(TEST_TASK_OPEN));
        List<TaskResponseDto> actual = taskService.getAvailableTasks();
        assertEquals(1, actual.size());
    }

    @Test
    void getTasksByUserIdTest() {
        when(mockTaskRepository.findByUserId(TEST_USER.getId()))
                .thenReturn(Optional.of(TEST_TASK_OPEN));
        List<TaskResponseDto> actual = taskService.getTasksByUserId(TEST_USER.getId());
        assertEquals(1, actual.size());

    }

    //TODO найти ошибку!!! ;
    @Test
    void getTasksByPerformerId() {

        List<Task> tasks = List.of(TEST_TASK2_IN_PROGRESS);
        Long performerId = TEST_PERFORMER2.getId();

        when(mockTaskRepository.findTasksByPerformerId(anyLong()))
                .thenReturn(tasks);


        List<TaskResponseDto> actual = taskService.getTasksByPerformerId(performerId);
        assertEquals(1, actual.size());

    }

    @Test
    void addPerformerToTask() {
        Performer performer = TEST_PERFORMER2;
        Long taskId = TEST_TASK_OPEN.getId();
        Long performerId = TEST_PERFORMER2.getId();
        when(mockPerformerRepository.findById(performerId))
                .thenReturn(Optional.of(TEST_PERFORMER2));
        TaskResponseDto actual = taskService.addPerformerToTask(taskId, performerId);
        assertEquals(TASK_RESPONSE_DTO_WITH_PERFORMER.getPerformer(), actual.getPerformer());
        assertEquals(TASK_RESPONSE_DTO_WITH_PERFORMER.getTaskStatus(), actual.getTaskStatus());

    }

    @Test
    void removePerformerFromTask() {
        when(mockTaskRepository.findById(any()))
                .thenReturn(Optional.of(TEST_TASK2_IN_PROGRESS));
        when(mockPerformerRepository.findById(TEST_PERFORMER2.getId()))
                .thenReturn(Optional.of(TEST_PERFORMER2));
        TaskResponseDto actual = taskService.removePerformerFromTask(TEST_TASK2_IN_PROGRESS.getId());
        assertNull(actual.getPerformer());
    }

    @Test
    void updateTaskStatusById() {
        Task task = TEST_TASK_OPEN.toBuilder().build();
        Task updatedTask = task.toBuilder()
                .taskStatus(TaskStatus.IN_PROGRESS)
                .build();
        when(mockTaskRepository.findById(any())).thenReturn(Optional.of(task));
        when(mockTaskRepository.save(eq(updatedTask))).thenReturn(updatedTask);
        TaskResponseDto actual = taskService
                .updateTaskStatusById(task.getId(), TaskStatus.IN_PROGRESS);
        assertEquals(TaskStatus.IN_PROGRESS, actual.getTaskStatus());

    }

    @Test
    void createTaskByUserId() {
        Long userId = TEST_USER.getId();
        TaskRequestDto requestDto = TASK_REQUEST_DTO;
        when(mockTaskRepository.save(any())).thenReturn(TEST_TASK_OPEN);
        TaskResponseDto actual = taskService.createTaskByUserId(userId, requestDto);
        assertEquals(TASK_RESPONSE_DTO.getUser(), actual.getUser());
    }
}