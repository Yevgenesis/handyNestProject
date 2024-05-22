package codezilla.handynestproject.controller;

import codezilla.handynestproject.HandyNestProjectApplication;
import codezilla.handynestproject.dto.performer.PerformerRequestDto;
import codezilla.handynestproject.dto.performer.PerformerResponseDto;
import codezilla.handynestproject.service.PerformerService;
import codezilla.handynestproject.util.TestDatabaseConfig;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static codezilla.handynestproject.service.TestData.PERFORMER_REQUEST_DTO1;
import static codezilla.handynestproject.service.TestData.PERFORMER_REQUEST_DTO3;
import static codezilla.handynestproject.service.TestData.PERFORMER_RESPONSE_DTO1;
import static codezilla.handynestproject.service.TestData.PERFORMER_RESPONSE_DTO2;
import static codezilla.handynestproject.service.TestData.PERFORMER_RESPONSE_DTO3;
import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
@Testcontainers
@ContextConfiguration(classes = {TestDatabaseConfig.class, HandyNestProjectApplication.class})
class PerformerControllerTest {


    private final PerformerService performerService;

    @Autowired
   PerformerControllerTest (PerformerService performerService) {
        this.performerService = performerService;
    };

    //не пойму как правильно прописать. Так как не совпадают User, isAvailable и дата
    @Test
    @SneakyThrows
    void createPerformerTest() {

        PerformerRequestDto performer = PERFORMER_REQUEST_DTO3;
        performer.setUserId(6L);
        PerformerResponseDto expected = PERFORMER_RESPONSE_DTO3;
        expected.setId(6L);
        PerformerResponseDto actual = performerService.create(performer);
        assertEquals(expected, actual);

    }

    @Test
    @SneakyThrows
    void updatePerformerTest()  {

        PerformerResponseDto expected = PERFORMER_RESPONSE_DTO1;

        PerformerResponseDto actual =
                performerService.update(PERFORMER_REQUEST_DTO1);
            assertEquals(expected, actual);
    }

    @Test
    @SneakyThrows
    void getAllPerformersTest() {

        List<PerformerResponseDto> actual = performerService.findAll();
        assertEquals(5, actual.size());
    }

    @Test
    @SneakyThrows
    void getPerformerByIdTest(){
        PerformerResponseDto expected = PERFORMER_RESPONSE_DTO2;
        Long id = expected.getId();
        PerformerResponseDto actual = performerService.findById(id);
        assertEquals(expected, actual);
    }
}