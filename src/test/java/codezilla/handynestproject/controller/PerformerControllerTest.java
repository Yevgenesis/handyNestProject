package codezilla.handynestproject.controller;

import codezilla.handynestproject.HandyNestProjectApplication;
import codezilla.handynestproject.dto.performer.PerformerRequestDto;
import codezilla.handynestproject.dto.performer.PerformerResponseDto;
import codezilla.handynestproject.dto.performer.PerformerUpdateRequestDto;
import codezilla.handynestproject.service.PerformerService;
import codezilla.handynestproject.testData.AddressTestData;
import codezilla.handynestproject.testData.CategoryTestData;
import codezilla.handynestproject.util.TestDatabaseConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static codezilla.handynestproject.testData.PerformerTestData.PERFORMER_REQUEST_DTO1;
import static codezilla.handynestproject.testData.PerformerTestData.PERFORMER_REQUEST_DTO3;
import static codezilla.handynestproject.testData.PerformerTestData.PERFORMER_REQUEST_DTO4;
import static codezilla.handynestproject.testData.PerformerTestData.PERFORMER_REQUEST_DTO5;
import static codezilla.handynestproject.testData.PerformerTestData.PERFORMER_RESPONSE_DTO1;
import static codezilla.handynestproject.testData.PerformerTestData.PERFORMER_RESPONSE_DTO3;
import static codezilla.handynestproject.testData.PerformerTestData.PERFORMER_RESPONSE_DTO5;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(classes = {TestDatabaseConfig.class, HandyNestProjectApplication.class})
class PerformerControllerTest {

    @Autowired
    private PerformerService performerService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Transactional
    @SneakyThrows
    void createTest() {
        PerformerResponseDto expected = PERFORMER_RESPONSE_DTO1;
        var result = mockMvc.perform(MockMvcRequestBuilders
                        .post("/performers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(PERFORMER_REQUEST_DTO1))
                        .characterEncoding("UTF-8"))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        PerformerResponseDto actual = objectMapper.readValue(jsonResponse, PerformerResponseDto.class);

        assertNotNull(actual.getId());
        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getLastName(), actual.getLastName());
        assertEquals(expected.getAddress(), actual.getAddress());

    }

    @Test
    @SneakyThrows
    void createExistingUserIdTest() {
        PerformerRequestDto performer = PERFORMER_REQUEST_DTO4;
        performer.setUserId(999L);
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/performers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(performer))
                        .characterEncoding("UTF-8"))
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    @SneakyThrows
    void createExistingCategoryIdTest() {
        PerformerRequestDto performer = PERFORMER_REQUEST_DTO3;
        performer.setCategoryIDs(List.of(999L));
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/performers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(performer))
                        .characterEncoding("UTF-8"))
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    @Transactional
    @SneakyThrows
    void updateTest() {
        PerformerUpdateRequestDto performer = new PerformerUpdateRequestDto(
                2L, "+49111111111",
                "Опытный маляр, предоставляю услуги качественной покраски стен", List.of(2L, 4L),
                AddressTestData.TEST_ADDRESS_DTO2);

        PerformerResponseDto expected = new PerformerResponseDto(
                2L, "Джейн", "Смит", "+49111111111",
                "Опытный маляр, предоставляю услуги качественной покраски стен",
                Set.of(CategoryTestData.CATEGORY_TITLE_DTO2, CategoryTestData.CATEGORY_TITLE_DTO4),
                AddressTestData.TEST_ADDRESS_DTO2, true, 4.8, 150L,
                Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                        (2024, 4, 29), LocalTime.of(13, 0, 0))),
                Timestamp.valueOf(LocalDateTime.of(LocalDate.of
                        (2024, 4, 29), LocalTime.of(13, 0, 0))));


        var result = mockMvc.perform(MockMvcRequestBuilders
                        .put("/performers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(performer))
                        .characterEncoding("UTF-8"))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        PerformerResponseDto actual = objectMapper.readValue(jsonResponse, PerformerResponseDto.class);
        assertNotNull(actual.getId());
        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getLastName(), actual.getLastName());
        assertEquals(expected.getAddress(), actual.getAddress());

    }

    @Test
    @SneakyThrows
    void updateExistingPerformerIdTest() {
        PerformerUpdateRequestDto performer = new PerformerUpdateRequestDto(
                999L, "+49111111111",
                "Опытный маляр, предоставляю услуги качественной покраски стен", List.of(2L, 4L),
                AddressTestData.TEST_ADDRESS_DTO2);

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/performers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(performer))
                        .characterEncoding("UTF-8"))
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    @SneakyThrows
    void findAllTest() {
        mockMvc.perform(get("/performers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(5))
                .andReturn();
    }

    @Test
    @SneakyThrows
    void findByIdTest() {
        PerformerResponseDto expected = PERFORMER_RESPONSE_DTO3;
        Long id = expected.getId();
        mockMvc.perform(get("/performers/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expected)));
    }

    @Test
    @SneakyThrows
    void findByIdExistingIdTest() {

        Long existingId = 999L;
        mockMvc.perform(get("/performers/{id}", existingId))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    @SneakyThrows
    void updateAvailabilityTest() {
        PerformerResponseDto expected = PERFORMER_RESPONSE_DTO5;
        Long id = expected.getId();
        boolean isAvailable = expected.isAvailable();

        var result = mockMvc.perform(put("/performers/{id}/{isAvailable}", id, isAvailable)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(PERFORMER_REQUEST_DTO5))
                        .characterEncoding("UTF-8"))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        PerformerResponseDto actual = objectMapper.readValue(jsonResponse, PerformerResponseDto.class);

        assertNotNull(actual.getId());
        assertEquals(expected.isAvailable(), actual.isAvailable());
    }

    @Test
    @SneakyThrows
    void updateAvailabilityExistingIdTest() {

        Long existingId = 999L;
        mockMvc.perform(put("/performers/{id}/{isAvailable}", existingId, true))
                .andExpect(status().isNotFound());
    }
}