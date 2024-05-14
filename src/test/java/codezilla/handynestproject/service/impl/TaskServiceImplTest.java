package codezilla.handynestproject.service.impl;

import codezilla.handynestproject.dto.task.TaskRequestDto;
import codezilla.handynestproject.dto.task.TaskResponseDto;
import codezilla.handynestproject.dto.task.TaskUpdateRequestDto;
import codezilla.handynestproject.mapper.AddressMapper;
import codezilla.handynestproject.mapper.CategoryMapper;
import codezilla.handynestproject.mapper.TaskMapper;
import codezilla.handynestproject.model.entity.Task;
import codezilla.handynestproject.model.enums.TaskStatus;
import codezilla.handynestproject.repository.CategoryRepository;
import codezilla.handynestproject.repository.PerformerRepository;
import codezilla.handynestproject.repository.TaskRepository;
import codezilla.handynestproject.repository.UserRepository;
import codezilla.handynestproject.repository.WorkingTimeRepository;
import codezilla.handynestproject.service.CategoryService;
import codezilla.handynestproject.service.TaskService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static codezilla.handynestproject.service.TestData.ADDRESS_DTO;
import static codezilla.handynestproject.service.TestData.CATEGORY_TITLE_DTO;
import static codezilla.handynestproject.service.TestData.TASK_REQUEST_DTO;
import static codezilla.handynestproject.service.TestData.TASK_RESPONSE_DTO;
import static codezilla.handynestproject.service.TestData.TASK_RESPONSE_DTO_WITH_PERFORMER;
import static codezilla.handynestproject.service.TestData.TEST_CATEGORY;
import static codezilla.handynestproject.service.TestData.TEST_PERFORMER;
import static codezilla.handynestproject.service.TestData.TEST_PERFORMER2;
import static codezilla.handynestproject.service.TestData.TEST_TASK2_IN_PROGRESS;
import static codezilla.handynestproject.service.TestData.TEST_TASK_OPEN;
import static codezilla.handynestproject.service.TestData.TEST_USER;
import static codezilla.handynestproject.service.TestData.TEST_WORKING_TIME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

/**
 * Если запустить тесты на весь класс, то произойдет ошибка во многих тестах,
 * так как тест updateTaskStatus(), меняет статус на IN_PROGRESS.
 * Тесты запускать только по одному
 */
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
        when(mockPerformerRepository.findById(any()))
                .thenReturn(Optional.ofNullable(TEST_PERFORMER));
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
        TaskResponseDto actual = taskService.getTaskById(TEST_TASK2_IN_PROGRESS.getId());
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
        List<Task> tasks = List.of(TEST_TASK_OPEN,TEST_TASK2_IN_PROGRESS);
        Long performerId = TEST_PERFORMER2.getId();
        when(mockPerformerRepository.findById(anyLong()))
                .thenReturn(Optional.of(TEST_PERFORMER2));
        when(mockTaskRepository.findAll())
                .thenReturn(tasks);
        when(mockCategoryMapper.categoryToTitleDto(TEST_CATEGORY))
                .thenReturn(CATEGORY_TITLE_DTO);
        when(mockTaskMapper.toTaskResponseDtoList(tasks))
                .thenReturn(List.of(TASK_RESPONSE_DTO_WITH_PERFORMER));

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
        assertNull(actual.getPerformer());
    }

    @Test
    void updateTaskStatusById() {
        Long taskId = TEST_TASK_OPEN.getId();
        TaskResponseDto actual = taskService.updateTaskStatusById(taskId, TaskStatus.IN_PROGRESS);
        assertEquals(TaskStatus.IN_PROGRESS, actual.getTaskStatus());
        /**
         * actual1 переменная для того чтобы вернуть статус OPEN, так как он нужен для других тестов
         */
        TaskResponseDto actual1 = taskService.updateTaskStatusById(taskId, TaskStatus.OPEN);
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