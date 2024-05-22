package codezilla.handynestproject.service.impl;

import codezilla.handynestproject.HandyNestProjectApplication;
import codezilla.handynestproject.dto.task.TaskResponseDto;
import codezilla.handynestproject.dto.task.TaskUpdateRequestDto;
import codezilla.handynestproject.model.entity.Task;
import codezilla.handynestproject.service.TaskService;
import codezilla.handynestproject.util.TestDatabaseConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

import static codezilla.handynestproject.service.TestData.TASK_REQUEST_DTO1;
import static codezilla.handynestproject.service.TestData.TASK_RESPONSE_DTO1;
import static codezilla.handynestproject.service.TestData.TEST_ADDRESS_DTO1;
import static codezilla.handynestproject.service.TestData.TEST_TASK1;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Testcontainers
@ContextConfiguration(classes = {TestDatabaseConfig.class, HandyNestProjectApplication.class})
class TaskServiceImplTest {

    private final TaskService taskService;

    @Autowired
    TaskServiceImplTest(TaskService taskService) {
        this.taskService = taskService;
    }

    @Test
    void createTest() {

        TaskResponseDto actual = taskService.create(TASK_REQUEST_DTO1);
        assertEquals(TASK_RESPONSE_DTO1, actual);
    }


    @Test
    void updateTest() {
        TaskUpdateRequestDto dto = new TaskUpdateRequestDto(
                1L,
                "Починить кран",
                "Требуется починить кран на кухне",
                50.0,
                TEST_ADDRESS_DTO1,
                1L);


        TaskResponseDto actual = taskService.update(dto);
        assertEquals(TASK_RESPONSE_DTO1, actual);
    }


    //TODO проверить написание теста

    @Test
    void cancelByIdTest() {

        Long taskId = TEST_TASK1.getId();


    }

    @Test
    void findByIdTest() {
        Task task = TEST_TASK1;
    }


    @Test
    void findAllTasksTest() {

    }


    @Test
    void findByStatusTest() {



    }

    @Test
    void findAvailableTasksTest() {

    }

    @Test
    void findByUserIdTest() {

    }

    //TODO найти ошибку!!! ;
    @Test
    void findByPerformerId() {



    }

    @Test
    void addPerformer() {

    }

    @Test
    void removePerformer() {
    }

    @Test
    void updateStatusById() {


    }

    @Test
    void createByUserId() {

    }
}