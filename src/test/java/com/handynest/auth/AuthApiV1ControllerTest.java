package com.handynest.auth;

import com.handynest.HandyNestProjectApplication;
import com.handynest.testsupport.TestDatabaseConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.handynest.auth.web.RefreshTokenCookieService;
import jakarta.servlet.http.Cookie;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "app.security.rate-limit.refresh.capacity=3",
        "app.security.rate-limit.refresh.window=1m"
})
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(classes = {TestDatabaseConfig.class, HandyNestProjectApplication.class})
class AuthApiV1ControllerTest {

    private static final String PASSWORD = "Test121314#";
    private static final AtomicInteger CLIENT_IP_SEQUENCE = new AtomicInteger(1);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerCreatesSessionWithoutExposingInternalUserId() throws Exception {
        String email = newEmail();

        mockMvc.perform(withClientIp(post("/api/v1/auth/register"), newClientIp())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerPayload(email))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.refreshToken").isString())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresInSeconds").value(900))
                .andExpect(jsonPath("$.user.publicId").isString())
                .andExpect(jsonPath("$.user.email").value(email))
                .andExpect(jsonPath("$.user.roles[0]").value("USER"))
                .andExpect(jsonPath("$.user.id").doesNotExist())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString(
                        RefreshTokenCookieService.DEFAULT_COOKIE_NAME + "="
                )))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("SameSite=Lax")));
    }

    @Test
    void registerValidationErrorReturnsUnifiedBadRequestContract() throws Exception {
        mockMvc.perform(withClientIp(post("/api/v1/auth/register"), newClientIp())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "firstName", "",
                                "lastName", "User",
                                "email", "not-an-email",
                                "password", "short",
                                "passwordConfirmation", "different"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.messages").doesNotExist());
    }

    @Test
    void loginCreatesSessionWithoutAuthentication() throws Exception {
        String email = newEmail();
        register(email);

        mockMvc.perform(withClientIp(post("/api/v1/auth/login"), newClientIp())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email,
                                "password", PASSWORD
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.refreshToken").isString())
                .andExpect(jsonPath("$.user.email").value(email))
                .andExpect(jsonPath("$.user.id").doesNotExist())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")));
    }

    @Test
    void meReturnsCurrentAuthenticatedUser() throws Exception {
        String email = newEmail();
        JsonNode registration = register(email);
        String accessToken = registration.get("accessToken").asText();

        mockMvc.perform(get("/api/v1/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.publicId").value(registration.get("user").get("publicId").asText()))
                .andExpect(jsonPath("$.id").doesNotExist());
    }

    @Test
    void refreshRotatesRefreshTokenAndRejectsOldToken() throws Exception {
        JsonNode registration = register(newEmail());
        String oldRefreshToken = registration.get("refreshToken").asText();

        JsonNode refreshed = objectMapper.readTree(mockMvc.perform(withClientIp(
                                post("/api/v1/auth/refresh"),
                                newClientIp()
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", oldRefreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.refreshToken").isString())
                .andReturn()
                .getResponse()
                .getContentAsString());

        assertThat(refreshed.get("refreshToken").asText()).isNotEqualTo(oldRefreshToken);

        mockMvc.perform(withClientIp(post("/api/v1/auth/refresh"), newClientIp())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", oldRefreshToken))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void refreshCanRotateRefreshTokenFromHttpOnlyCookie() throws Exception {
        JsonNode registration = register(newEmail());
        String oldRefreshToken = registration.get("refreshToken").asText();

        JsonNode refreshed = objectMapper.readTree(mockMvc.perform(withClientIp(
                                post("/api/v1/auth/refresh"),
                                newClientIp()
                        )
                        .cookie(refreshCookie(oldRefreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.refreshToken").isString())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString(
                        RefreshTokenCookieService.DEFAULT_COOKIE_NAME + "="
                )))
                .andReturn()
                .getResponse()
                .getContentAsString());

        assertThat(refreshed.get("refreshToken").asText()).isNotEqualTo(oldRefreshToken);

        mockMvc.perform(withClientIp(post("/api/v1/auth/refresh"), newClientIp())
                        .cookie(refreshCookie(oldRefreshToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void logoutRevokesRefreshToken() throws Exception {
        JsonNode registration = register(newEmail());
        String accessToken = registration.get("accessToken").asText();
        String refreshToken = registration.get("refreshToken").asText();

        mockMvc.perform(withClientIp(post("/api/v1/auth/logout"), newClientIp())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isNoContent());

        mockMvc.perform(withClientIp(post("/api/v1/auth/refresh"), newClientIp())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void logoutCanRevokeRefreshTokenFromCookieAndClearCookie() throws Exception {
        JsonNode registration = register(newEmail());
        String accessToken = registration.get("accessToken").asText();
        String refreshToken = registration.get("refreshToken").asText();

        mockMvc.perform(withClientIp(post("/api/v1/auth/logout"), newClientIp())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .cookie(refreshCookie(refreshToken)))
                .andExpect(status().isNoContent())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString(
                        RefreshTokenCookieService.DEFAULT_COOKIE_NAME + "="
                )))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")));

        mockMvc.perform(withClientIp(post("/api/v1/auth/refresh"), newClientIp())
                        .cookie(refreshCookie(refreshToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void registerIsRateLimitedByClientIp() throws Exception {
        String clientIp = newClientIp();

        for (int i = 0; i < 3; i++) {
            mockMvc.perform(withClientIp(post("/api/v1/auth/register"), clientIp)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerPayload(newEmail()))))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(withClientIp(post("/api/v1/auth/register"), clientIp)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerPayload(newEmail()))))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists(HttpHeaders.RETRY_AFTER))
                .andExpect(jsonPath("$.code").value("RATE_LIMITED"))
                .andExpect(jsonPath("$.message").value("Too many requests"));
    }

    @Test
    void loginIsRateLimitedByClientIpAndEmail() throws Exception {
        String email = newEmail();
        String clientIp = newClientIp();
        register(email, newClientIp());

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(withClientIp(post("/api/v1/auth/login"), clientIp)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "email", email,
                                    "password", "Wrong121314#"
                            ))))
                    .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(withClientIp(post("/api/v1/auth/login"), clientIp)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email,
                                "password", "Wrong121314#"
                        ))))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists(HttpHeaders.RETRY_AFTER))
                .andExpect(jsonPath("$.code").value("RATE_LIMITED"));
    }

    @Test
    void refreshIsRateLimitedByClientIp() throws Exception {
        String clientIp = newClientIp();
        JsonNode registration = register(newEmail(), newClientIp());
        String refreshToken = registration.get("refreshToken").asText();

        for (int i = 0; i < 3; i++) {
            int status = mockMvc.perform(withClientIp(post("/api/v1/auth/refresh"), clientIp)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
                    .andReturn()
                    .getResponse()
                    .getStatus();

            assertThat(status).isIn(200, 401);
        }

        mockMvc.perform(withClientIp(post("/api/v1/auth/refresh"), clientIp)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists(HttpHeaders.RETRY_AFTER))
                .andExpect(jsonPath("$.code").value("RATE_LIMITED"));
    }

    @Test
    void meWithoutTokenReturnsJsonUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    void meWithMalformedBearerTokenReturnsJsonUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer malformed-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void publicCategoriesStayAvailableWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk());
    }

    @Test
    void publicGeoEndpointsStayAvailableWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/geo/countries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("KZ"));
    }

    private JsonNode register(String email) throws Exception {
        return register(email, newClientIp());
    }

    private JsonNode register(String email, String clientIp) throws Exception {
        MvcResult result = mockMvc.perform(withClientIp(post("/api/v1/auth/register"), clientIp)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerPayload(email))))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private Map<String, String> registerPayload(String email) {
        return Map.of(
                "firstName", "Test",
                "lastName", "User",
                "email", email,
                "password", PASSWORD,
                "passwordConfirmation", PASSWORD
        );
    }

    private String newEmail() {
        return "auth" + UUID.randomUUID().toString().replace("-", "").substring(0, 16) + "@e.kz";
    }

    private Cookie refreshCookie(String refreshToken) {
        return new Cookie(RefreshTokenCookieService.DEFAULT_COOKIE_NAME, refreshToken);
    }

    private MockHttpServletRequestBuilder withClientIp(
            MockHttpServletRequestBuilder requestBuilder,
            String clientIp
    ) {
        return requestBuilder.header("X-Forwarded-For", clientIp);
    }

    private String newClientIp() {
        int value = CLIENT_IP_SEQUENCE.getAndIncrement();
        return "10.0." + (value / 250) + "." + ((value % 250) + 1);
    }
}
