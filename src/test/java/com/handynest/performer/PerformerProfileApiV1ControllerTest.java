package com.handynest.performer;

import codezilla.handynestproject.HandyNestProjectApplication;
import codezilla.handynestproject.util.TestDatabaseConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(classes = {TestDatabaseConfig.class, HandyNestProjectApplication.class})
class PerformerProfileApiV1ControllerTest {

    private static final String PASSWORD = "Test121314#";
    private static final String ALMATY_CITY_PUBLIC_ID = "06KZCT00000000000000000001";
    private static final String REPAIR_CATEGORY_PUBLIC_ID = "06F5Z8NMS7YQE4Q2F4R1HN9EZG";
    private static final AtomicInteger CLIENT_IP_SEQUENCE = new AtomicInteger(151);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void createGetPatchAndAvailabilityWorkForCurrentUser() throws Exception {
        String accessToken = register(newEmail()).get("accessToken").asText();

        JsonNode created = objectMapper.readTree(mockMvc.perform(post("/api/v1/performers/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(performerPayload("Handy Pro"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.publicId").isString())
                .andExpect(jsonPath("$.id").doesNotExist())
                .andExpect(jsonPath("$.displayName").value("Handy Pro"))
                .andExpect(jsonPath("$.countryCode").value("KZ"))
                .andExpect(jsonPath("$.cityId").value(ALMATY_CITY_PUBLIC_ID))
                .andExpect(jsonPath("$.cityName").value("Алматы"))
                .andExpect(jsonPath("$.worksRemotely").value(true))
                .andExpect(jsonPath("$.worksOnsite").value(true))
                .andExpect(jsonPath("$.verificationLevel").value("NONE"))
                .andExpect(jsonPath("$.verificationStatus").value("NOT_SUBMITTED"))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.categories[0].categoryId").value(REPAIR_CATEGORY_PUBLIC_ID))
                .andExpect(jsonPath("$.categories[0].title").value("Ремонт"))
                .andExpect(jsonPath("$.categories[0].currency").value("KZT"))
                .andReturn()
                .getResponse()
                .getContentAsString());

        String performerId = created.get("publicId").asText();

        mockMvc.perform(get("/api/v1/performers/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publicId").value(performerId));

        mockMvc.perform(patch("/api/v1/performers/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(performerPayload("Handy Pro Updated"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Handy Pro Updated"));

        mockMvc.perform(post("/api/v1/performers/me/availability")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("available", false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    void duplicatePerformerProfileReturnsConflict() throws Exception {
        String accessToken = register(newEmail()).get("accessToken").asText();

        mockMvc.perform(post("/api/v1/performers/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(performerPayload("Duplicate One"))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/performers/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(performerPayload("Duplicate Two"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_RESOURCE"));
    }

    @Test
    void createWithoutCategoriesReturnsBadRequest() throws Exception {
        String accessToken = register(newEmail()).get("accessToken").asText();

        Map<String, Object> payload = performerPayload("No Categories");
        payload.put("categories", List.of());

        mockMvc.perform(post("/api/v1/performers/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    void createWithUnknownCityReturnsNotFound() throws Exception {
        String accessToken = register(newEmail()).get("accessToken").asText();

        Map<String, Object> payload = performerPayload("Unknown City");
        payload.put("cityId", "06KZCT00000000000000000999");

        mockMvc.perform(post("/api/v1/performers/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void publicDetailAndSearchAreAvailableWithoutToken() throws Exception {
        String accessToken = register(newEmail()).get("accessToken").asText();

        JsonNode created = objectMapper.readTree(mockMvc.perform(post("/api/v1/performers/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(performerPayload("Public Pro"))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString());

        String performerId = created.get("publicId").asText();

        mockMvc.perform(get("/api/v1/performers/{performerId}", performerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publicId").value(performerId))
                .andExpect(jsonPath("$.id").doesNotExist());

        mockMvc.perform(get("/api/v1/performers")
                        .queryParam("categoryId", REPAIR_CATEGORY_PUBLIC_ID)
                        .queryParam("cityId", ALMATY_CITY_PUBLIC_ID)
                        .queryParam("isAvailable", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].publicId", hasItem(performerId)))
                .andExpect(jsonPath("$.content[0].id").doesNotExist());
    }

    @Test
    void categoryVerificationRequirementReturnsVerificationRequired() throws Exception {
        jdbcTemplate.update(
                "UPDATE category SET requires_verification_level = 'PHONE_VERIFIED' WHERE public_id = ?",
                REPAIR_CATEGORY_PUBLIC_ID
        );

        try {
            String accessToken = register(newEmail()).get("accessToken").asText();

            mockMvc.perform(post("/api/v1/performers/me")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(performerPayload("Needs Verification"))))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("VERIFICATION_REQUIRED"));
        } finally {
            jdbcTemplate.update(
                    "UPDATE category SET requires_verification_level = 'NONE' WHERE public_id = ?",
                    REPAIR_CATEGORY_PUBLIC_ID
            );
        }
    }

    private Map<String, Object> performerPayload(String displayName) {
        return new java.util.LinkedHashMap<>(Map.of(
                "displayName", displayName,
                "description", "Профиль исполнителя для интеграционного теста",
                "skillsDescription", "Ремонт, диагностика, мелкие работы",
                "cityId", ALMATY_CITY_PUBLIC_ID,
                "serviceRadiusKm", 30,
                "worksRemotely", true,
                "worksOnsite", true,
                "travelFeePolicy", "included",
                "categories", List.of(categoryPayload())
        ));
    }

    private Map<String, Object> categoryPayload() {
        return Map.of(
                "categoryId", REPAIR_CATEGORY_PUBLIC_ID,
                "experienceYears", 3,
                "priceFrom", 1000,
                "priceTo", 5000,
                "currency", "KZT",
                "primary", true
        );
    }

    private JsonNode register(String email) throws Exception {
        MvcResult result = mockMvc.perform(withClientIp(post("/api/v1/auth/register"), newClientIp())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "firstName", "Test",
                                "lastName", "Performer",
                                "email", email,
                                "password", PASSWORD,
                                "passwordConfirmation", PASSWORD
                        ))))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private MockHttpServletRequestBuilder withClientIp(
            MockHttpServletRequestBuilder requestBuilder,
            String clientIp
    ) {
        return requestBuilder.header("X-Forwarded-For", clientIp);
    }

    private String newEmail() {
        return "perf" + UUID.randomUUID().toString().replace("-", "").substring(0, 18) + "@e.kz";
    }

    private String newClientIp() {
        return "198.51.100." + CLIENT_IP_SEQUENCE.getAndIncrement();
    }
}
