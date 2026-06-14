package com.handynest.identity;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.handynest.HandyNestProjectApplication;
import com.handynest.testsupport.AuthTestPayloads;
import com.handynest.testsupport.TestDatabaseConfig;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(classes = {TestDatabaseConfig.class, HandyNestProjectApplication.class})
class UserProfileApiV1ControllerTest {

  private static final String PASSWORD = "Test121314#";
  private static final String TASHKENT_CITY_PUBLIC_ID = "06UZCT00000000000000000001";
  private static final AtomicInteger CLIENT_IP_SEQUENCE = new AtomicInteger(101);

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void meRequiresAuthentication() throws Exception {
    mockMvc
        .perform(get("/api/v1/users/me"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
  }

  @Test
  void registerCreatesCustomerProfileAndMeDoesNotExposeInternalIds() throws Exception {
    JsonNode registration = register(newEmail());
    String accessToken = registration.get("accessToken").asText();

    mockMvc
        .perform(get("/api/v1/users/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.publicId").value(registration.get("user").get("publicId").asText()))
        .andExpect(jsonPath("$.id").doesNotExist())
        .andExpect(jsonPath("$.customerProfile").exists())
        .andExpect(jsonPath("$.customerProfile.ratingAverage").value(0.00))
        .andExpect(jsonPath("$.businessProfile").doesNotExist())
        .andExpect(jsonPath("$.status").value("ACTIVE"))
        .andExpect(jsonPath("$.accountType").value("PERSONAL"));
  }

  @Test
  void patchMeUpdatesPersonalFieldsGeoAndBusinessProfile() throws Exception {
    JsonNode registration = register(newEmail());
    String accessToken = registration.get("accessToken").asText();

    Map<String, Object> payload =
        Map.of(
            "firstName", "Aruzhan",
            "lastName", "Test",
            "phone", "+998901234567",
            "accountType", "BUSINESS",
            "cityId", TASHKENT_CITY_PUBLIC_ID,
            "preferredServiceRadiusKm", 25,
            "businessProfile",
                Map.of(
                    "companyName", "Handy Business",
                    "bin", "123456789012",
                    "legalAddress", "Tashkent",
                    "billingEmail", "billing@example.com",
                    "contactPersonName", "Aruzhan",
                    "contactPersonPhone", "+998901234567"));

    mockMvc
        .perform(
            patch("/api/v1/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.firstName").value("Aruzhan"))
        .andExpect(jsonPath("$.phone").value("+998901234567"))
        .andExpect(jsonPath("$.phoneVerified").value(false))
        .andExpect(jsonPath("$.accountType").value("BUSINESS"))
        .andExpect(jsonPath("$.cityId").value(TASHKENT_CITY_PUBLIC_ID))
        .andExpect(jsonPath("$.cityName").value("Ташкент"))
        .andExpect(jsonPath("$.countryCode").value("UZ"))
        .andExpect(jsonPath("$.preferredServiceRadiusKm").value(25))
        .andExpect(jsonPath("$.businessProfile.companyName").value("Handy Business"))
        .andExpect(jsonPath("$.businessProfile.verificationStatus").value("NOT_SUBMITTED"))
        .andExpect(jsonPath("$.businessProfile.id").doesNotExist());
  }

  @Test
  void deleteMeSoftDeletesAccountRevokesRefreshTokenAndBlocksOldAccessToken() throws Exception {
    String email = newEmail();
    JsonNode registration = register(email);
    String accessToken = registration.get("accessToken").asText();
    String refreshToken = registration.get("refreshToken").asText();

    mockMvc
        .perform(
            delete("/api/v1/users/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
        .andExpect(status().isNoContent())
        .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")));

    mockMvc
        .perform(get("/api/v1/users/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

    mockMvc
        .perform(
            withClientIp(post("/api/v1/auth/refresh"), newClientIp())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

    mockMvc
        .perform(
            withClientIp(post("/api/v1/auth/login"), newClientIp())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of(
                            "email", email,
                            "password", PASSWORD))))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
  }

  private JsonNode register(String email) throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                withClientIp(post("/api/v1/auth/register"), newClientIp())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            AuthTestPayloads.registrationPayload("Test", "User", email, PASSWORD))))
            .andExpect(status().isCreated())
            .andReturn();

    return objectMapper.readTree(result.getResponse().getContentAsString());
  }

  private MockHttpServletRequestBuilder withClientIp(
      MockHttpServletRequestBuilder requestBuilder, String clientIp) {
    return requestBuilder.header("X-Forwarded-For", clientIp);
  }

  private String newEmail() {
    return "user" + UUID.randomUUID().toString().replace("-", "").substring(0, 18) + "@e.kz";
  }

  private String newClientIp() {
    return "198.51.100." + CLIENT_IP_SEQUENCE.getAndIncrement();
  }
}
