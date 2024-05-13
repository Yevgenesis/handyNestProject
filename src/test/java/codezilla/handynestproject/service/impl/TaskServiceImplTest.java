package codezilla.handynestproject.service.impl;

import codezilla.handynestproject.dto.task.TaskRequestDto;
import codezilla.handynestproject.dto.task.TaskResponseDto;
import codezilla.handynestproject.dto.task.TaskUpdateRequestDto;
import codezilla.handynestproject.mapper.TaskMapper;
import codezilla.handynestproject.mapper.TaskMapperImpl;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static codezilla.handynestproject.service.TestData.TASK_REQUEST_DTO;
import static codezilla.handynestproject.service.TestData.TASK_RESPONSE_DTO;
import static codezilla.handynestproject.service.TestData.TASK_RESPONSE_DTO_WITH_PERFORMER;
import static codezilla.handynestproject.service.TestData.TEST_ADDRESS;
import static codezilla.handynestproject.service.TestData.TEST_CATEGORY;
import static codezilla.handynestproject.service.TestData.TEST_PERFORMER;
import static codezilla.handynestproject.service.TestData.TEST_PERFORMER2;
import static codezilla.handynestproject.service.TestData.TEST_TASK2_IN_PROGRESS;
import static codezilla.handynestproject.service.TestData.TEST_TASK_OPEN;
import static codezilla.handynestproject.service.TestData.TEST_USER;
import static codezilla.handynestproject.service.TestData.TEST_WORKING_TIME;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
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
    private TaskMapper taskMapper = new TaskMapperImpl();

    @Autowired
    private CategoryService categoryService;


    private MockMvc mockMvc;
    private TaskService taskService;


    @BeforeEach
    void setUp() {
        openMocks(this);
        taskService = new TaskServiceImpl(mockUserRepository, mockTaskRepository,
                mockWorkingTimeRepository, mockCategoryRepository, mockPerformerRepository, taskMapper,
                categoryService);
        when(mockTaskRepository.save(any())).thenReturn(TEST_TASK_OPEN);
        when(mockTaskRepository.findById(any()))
                .thenReturn(Optional.ofNullable(TEST_TASK_OPEN));
        when(mockWorkingTimeRepository.findById(any()))
                .thenReturn(Optional.ofNullable(TEST_WORKING_TIME));
        when(mockCategoryRepository.findById(any()))
                .thenReturn(Optional.ofNullable(TEST_CATEGORY));
        when(mockPerformerRepository.findById(any()))
                .thenReturn(Optional.ofNullable(TEST_PERFORMER));
        when(mockUserRepository.findById(any()))
                .thenReturn(Optional.ofNullable(TEST_USER));

    }


    @Test
    void createTaskTest() {
        when(mockTaskRepository.save(any())).thenReturn(TEST_TASK_OPEN);
        TaskResponseDto actual = taskService.createTask(TASK_REQUEST_DTO);
        assertEquals(TASK_RESPONSE_DTO, actual);
    }


    @Test
    void updateTaskTest() {
        TaskUpdateRequestDto dto = new TaskUpdateRequestDto(
                1L,
                "Test Title",
                "Test Description",
                0.0, TEST_ADDRESS,
                2L);

        assertEquals(TASK_RESPONSE_DTO, taskService.updateTask(dto));
    }


    //TODO проверить написание теста

    @Test
    void deleteTaskByIdTest() {

        Long taskId = TEST_TASK_OPEN.getId();
        doNothing().when(mockTaskRepository).deleteById(taskId);

    }


    @Test
    void getTaskByIdTest() {
        TaskResponseDto actual = taskService.getTaskById(TEST_TASK_OPEN.getId());
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
//TODO найти ошибку!!! ? actual = 0;
    @Test
    void getTasksByPerformerId() {
        List<Task> tasks = List.of(TEST_TASK_OPEN, TEST_TASK2_IN_PROGRESS);
        Long performerId = TEST_PERFORMER2.getId();
        when(mockPerformerRepository.findById(performerId))
        .thenReturn(Optional.of(TEST_PERFORMER2));
        when(mockTaskRepository.findAll())
                .thenReturn(tasks);

        List<TaskResponseDto> actual = taskService.getTasksByPerformerId(performerId);
        assertEquals(1, actual.size());

    }

    @Test
    void addPerformerToTask() {
        Long taskId = TASK_RESPONSE_DTO_WITH_PERFORMER.getId();
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
        assertEquals(TASK_RESPONSE_DTO, actual);
        assertEquals(TaskStatus.OPEN, actual.getTaskStatus());
        assertNull(actual.getPerformer());
    }

    @Test
    void updateTaskStatusById() {
        Long taskId = TEST_TASK_OPEN.getId();
        TaskResponseDto actual = taskService.updateTaskStatusById(taskId, TaskStatus.IN_PROGRESS);
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