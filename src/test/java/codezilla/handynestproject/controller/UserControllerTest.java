package codezilla.handynestproject.controller;

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

import static codezilla.handynestproject.service.TestData.USER_RESPONSE_DTO3;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Testcontainers
@ContextConfiguration(classes = {TestDatabaseConfig.class, HandyNestProjectApplication.class})
class UserControllerTest {

    private final UserService userService;

    @Autowired
    UserControllerTest(UserService userService) {
        this.userService = userService;
    }


    @Test
    @SneakyThrows
    void getAllUsersTest() {

       List<UserResponseDto> actual = userService.findAll();
       assertEquals(11, actual.size());
    }

    @Test
    @SneakyThrows
    void getUserByIdTest() {
        UserResponseDto expected = USER_RESPONSE_DTO3;
        Long id = expected.getId();

        UserResponseDto actual = userService.findById(id);
        assertEquals(expected, actual);
    }
}