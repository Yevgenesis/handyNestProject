package codezilla.handynestproject.service.impl;

import codezilla.handynestproject.HandyNestProjectApplication;
import codezilla.handynestproject.dto.user.UserResponseDto;
import codezilla.handynestproject.service.UserService;
import codezilla.handynestproject.util.TestDatabaseConfig;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static codezilla.handynestproject.service.TestData.USER_RESPONSE_DTO;
import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
@Testcontainers
@ContextConfiguration(classes = {TestDatabaseConfig.class, HandyNestProjectApplication.class})
class UserServiceImplTest {

    private final UserService userService;


    @Autowired
    UserServiceImplTest(UserService userService) {
        this.userService = userService;
    }


    @Test
    @SneakyThrows
    void getUsers() {
        List<UserResponseDto> users = userService.getUsers();
        assertEquals(11, users.size());
    }

    @Test
    @SneakyThrows
    void getUserById() {
        UserResponseDto user = userService.getUserById(1L);
        assertEquals(USER_RESPONSE_DTO, user);
    }
}