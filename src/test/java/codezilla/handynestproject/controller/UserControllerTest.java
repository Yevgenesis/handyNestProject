package codezilla.handynestproject.controller;

import codezilla.handynestproject.HandyNestProjectApplication;
import codezilla.handynestproject.dto.user.UserRequestDto;
import codezilla.handynestproject.dto.user.UserResponseDto;
import codezilla.handynestproject.service.UserService;
import codezilla.handynestproject.util.TestDatabaseConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;

import static codezilla.handynestproject.testData.UserTestData.USER_RESPONSE_DTO3;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(classes = {TestDatabaseConfig.class, HandyNestProjectApplication.class})
class UserControllerTest {

    @Autowired
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    @SneakyThrows
    @WithMockUser(authorities = "ADMIN")
    void findAllTest() {

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(12));
    }

    @Test
    @SneakyThrows
    @WithMockUser(authorities = "USER")
    void findByIdTest() {
        UserResponseDto expected = USER_RESPONSE_DTO3;
        mockMvc.perform(get("/users/" + expected.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expected.getId()))
                .andExpect(content().json(objectMapper.writeValueAsString(expected)));
    }

    @Test
    @SneakyThrows
    @WithMockUser(authorities = "USER")
    void findByIdExistingUserIdTest() {
        Long existingUserId = 999L;
        mockMvc.perform(get("/users/{id}", existingUserId))
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    @WithMockUser(authorities = "USER")
    void saveTest() {

        UserRequestDto requestDto = new UserRequestDto(
                "TestName", "TestLastName", "test.test@example.com",
                "Test121314#", "Test121314#");
        var result = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .characterEncoding("UTF-8"))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        UserResponseDto actual = objectMapper.readValue(jsonResponse, UserResponseDto.class);

        assertNotNull(actual.getId());
        assertNotNull(actual.getFirstName());
        assertNotNull(actual.getLastName());
        assertNotNull(actual.getEmail());

    }

    @Test
    @SneakyThrows
    @WithMockUser(authorities = "USER")
    void saveAlreadyExistingEmailTest() {

        UserRequestDto requestDto = new UserRequestDto(
                "Алиса", "Джонсон", "alice.johnson@example.com",
                "Test121314#", "Test121314#");
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .characterEncoding("UTF-8"))
                .andExpect(status().isConflict())
                .andReturn();
    }

    @Test
    @SneakyThrows
    @WithMockUser(authorities = "USER")
    void saveWrongConfirmationPasswordTest() {

        UserRequestDto requestDto = new UserRequestDto(
                "Алиса", "Джонсон", "test.johnson@example.com",
                "Test121314#", "Test12131#4");
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .characterEncoding("UTF-8"))
                .andExpect(status().isBadRequest())
                .andReturn();
    }
}