package codezilla.handynestproject.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TaskRepositoryTest {

    @Autowired
    TaskRepository taskRepository;

    @Test
    void findAll() {
    }

    @Test
    void findById() {
    }

    @Test
    void findByUserId() {
    }

    @Test
    void findTaskByTaskStatus() {
    }
}