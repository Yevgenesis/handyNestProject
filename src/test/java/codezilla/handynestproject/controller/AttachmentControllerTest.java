package codezilla.handynestproject.controller;

import codezilla.handynestproject.HandyNestProjectApplication;
import codezilla.handynestproject.dto.attachment.AttachmentDto;
import codezilla.handynestproject.dto.attachment.AttachmentRequestDto;
import codezilla.handynestproject.util.TestDatabaseConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static codezilla.handynestproject.testData.AttachmentTestData.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(classes = {TestDatabaseConfig.class, HandyNestProjectApplication.class})
class AttachmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @SneakyThrows
    @Transactional
    @WithMockUser(authorities = "PERFORMER")
    void createTest() {
        AttachmentRequestDto requestDto = TEST_ATTACHMENT_REQUEST_DTO6;
        AttachmentDto expectedDto = TEST_ATTACHMENT_DTO6;

        var result = mockMvc.perform(MockMvcRequestBuilders
                        .post("/attachments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .characterEncoding("UTF-8"))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        AttachmentDto actual = objectMapper.readValue(jsonResponse, AttachmentDto.class);
        assertEquals(expectedDto, actual);

    }

    @Test
    @SneakyThrows
    @WithMockUser(authorities = "ADMIN")
    void findAllTest() {
        mockMvc.perform(get("/attachments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(5));

    }

    @Test
    @SneakyThrows
    @WithMockUser(authorities = "ADMIN")
    void findByIdTest() {
        AttachmentDto expected = TEST_ATTACHMENT_DTO1;
        Long id = expected.getId();
        mockMvc.perform(get("/attachments/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expected)));

    }

    @Test
    @SneakyThrows
    @WithMockUser(authorities = "ADMIN")
    void findByPerformerIdTest() {
        Long id = TEST_ATTACHMENT_DTO1.getPerformerId();
        List<AttachmentDto> expectedAttachments = List.of(TEST_ATTACHMENT_DTO1);

        mockMvc.perform(get("/attachments/performer/{performerId}", id))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedAttachments)));
    }


    @Test
    @SneakyThrows
    @WithMockUser(authorities = "ADMIN")
    void deleteTest() {
        Long attachmentId = 5L;
        mockMvc.perform(delete("/attachments/{attachmentId}", attachmentId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/attachments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(4));

    }
}