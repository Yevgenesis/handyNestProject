package codezilla.handynestproject.service.impl;

import codezilla.handynestproject.HandyNestProjectApplication;
import codezilla.handynestproject.service.PerformerService;
import codezilla.handynestproject.util.TestDatabaseConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Testcontainers
@ContextConfiguration(classes = {TestDatabaseConfig.class, HandyNestProjectApplication.class})
class PerformerServiceImplTest {

    private final PerformerService performerService;

    @Autowired
    PerformerServiceImplTest(PerformerService performerService) {
        this.performerService = performerService;
    }


    @Test
    void createPerformer() {

    }

    @Test
    void updatePerformer() {
    }

    @Test
    void getPerformers() {
        var performers = performerService.getPerformers();
        assertEquals(5, performers.size());
    }

    @Test
    void getPerformerById() {
        var performer = performerService.getPerformerById(1L);
        assertNotNull(performer);
    }
}