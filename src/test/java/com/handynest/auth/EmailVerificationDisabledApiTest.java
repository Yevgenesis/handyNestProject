package com.handynest.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

@SpringBootTest(properties = "app.security.email-verification.enabled=false")
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(classes = {TestDatabaseConfig.class, HandyNestProjectApplication.class})
class EmailVerificationDisabledApiTest {

  private static final String PASSWORD = "Test121314#";
  private static final AtomicInteger CLIENT_IP_SEQUENCE = new AtomicInteger(1);

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void requestEmailVerificationReturnsBadRequestWhenDisabled() throws Exception {
    JsonNode registration = register(newEmail());

    mockMvc
        .perform(
            withClientIp(post("/api/v1/auth/request-email-verification"), newClientIp())
                .header(
                    HttpHeaders.AUTHORIZATION,
                    "Bearer " + registration.get("accessToken").asText()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
        .andExpect(jsonPath("$.message").value("Email verification is disabled"));
  }

  @Test
  void verifyEmailReturnsBadRequestWhenDisabled() throws Exception {
    mockMvc
        .perform(
            withClientIp(post("/api/v1/auth/verify-email"), newClientIp())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("token", "disabled-token"))))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
        .andExpect(jsonPath("$.message").value("Email verification is disabled"));
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

  private String newEmail() {
    return "disabled" + UUID.randomUUID().toString().replace("-", "").substring(0, 16) + "@e.kz";
  }

  private MockHttpServletRequestBuilder withClientIp(
      MockHttpServletRequestBuilder requestBuilder, String clientIp) {
    return requestBuilder.header("X-Forwarded-For", clientIp);
  }

  private String newClientIp() {
    int value = CLIENT_IP_SEQUENCE.getAndIncrement();
    return "10.10." + (value / 250) + "." + ((value % 250) + 1);
  }
}
