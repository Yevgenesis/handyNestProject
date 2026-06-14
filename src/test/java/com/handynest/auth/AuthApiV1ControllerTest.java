package com.handynest.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.handynest.HandyNestProjectApplication;
import com.handynest.auth.verification.EmailProvider;
import com.handynest.auth.verification.EmailVerificationToken;
import com.handynest.auth.verification.EmailVerificationTokenRepository;
import com.handynest.auth.verification.OtpCode;
import com.handynest.auth.verification.OtpCodeRepository;
import com.handynest.auth.verification.OtpPurpose;
import com.handynest.auth.verification.SmsProvider;
import com.handynest.auth.verification.VerificationHashService;
import com.handynest.auth.web.RefreshTokenCookieService;
import com.handynest.identity.UserConsentRepository;
import com.handynest.identity.UserRepository;
import com.handynest.testsupport.AuthTestPayloads;
import com.handynest.testsupport.TestDatabaseConfig;
import jakarta.servlet.http.Cookie;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(
    properties = {
      "app.security.rate-limit.refresh.capacity=3",
      "app.security.rate-limit.refresh.window=1m",
      "app.security.rate-limit.otp-request.capacity=3",
      "app.security.rate-limit.otp-request.window=10m",
      "app.security.rate-limit.otp-verify.capacity=5",
      "app.security.rate-limit.otp-verify.window=10m",
      "app.security.rate-limit.email-verification-request.capacity=3",
      "app.security.rate-limit.email-verification-request.window=1h",
      "app.security.email-verification.enabled=true"
    })
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(
    classes = {
      TestDatabaseConfig.class,
      HandyNestProjectApplication.class,
      AuthApiV1ControllerTest.VerificationProviderTestConfig.class
    })
class AuthApiV1ControllerTest {

  private static final String PASSWORD = "Test121314#";
  private static final String TASHKENT_CITY_PUBLIC_ID = "06UZCT00000000000000000001";
  private static final String REPAIR_CATEGORY_PUBLIC_ID = "06CAT000000000000000000011";
  private static final AtomicInteger CLIENT_IP_SEQUENCE = new AtomicInteger(1);

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private CapturingSmsProvider smsProvider;

  @Autowired private CapturingEmailProvider emailProvider;

  @Autowired private OtpCodeRepository otpCodeRepository;

  @Autowired private EmailVerificationTokenRepository emailVerificationTokenRepository;

  @Autowired private VerificationHashService hashService;

  @Autowired private UserRepository userRepository;

  @Autowired private UserConsentRepository userConsentRepository;

  @Test
  void registerCreatesSessionWithoutExposingInternalUserId() throws Exception {
    String email = newEmail();

    mockMvc
        .perform(
            withClientIp(post("/api/v1/auth/register"), newClientIp())
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
        .andExpect(
            header()
                .string(
                    HttpHeaders.SET_COOKIE,
                    containsString(RefreshTokenCookieService.DEFAULT_COOKIE_NAME + "=")))
        .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")))
        .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("SameSite=Lax")));

    Long userId = userRepository.findByEmail(email).orElseThrow().getId();
    assertThat(userConsentRepository.findAllByUserIdOrderByAcceptedAtDesc(userId)).hasSize(7);
  }

  @Test
  void registrationAcceptsCommonSpecialCharactersInPassword() throws Exception {
    Map<String, Object> payload =
        AuthTestPayloads.registrationPayload("Test", "User", newEmail(), "ValidPass1!");

    mockMvc
        .perform(
            withClientIp(post("/api/v1/auth/register"), newClientIp())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
        .andExpect(status().isCreated());
  }

  @Test
  void legalRequirementsArePublicAndExposeCurrentVersions() throws Exception {
    mockMvc
        .perform(get("/api/v1/legal/consent-requirements"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[?(@.type == 'TERMS_OF_SERVICE')].documentVersion").value("1.0"))
        .andExpect(
            jsonPath("$[?(@.type == 'TERMS_OF_SERVICE')].requiredAtRegistration").value(true))
        .andExpect(
            jsonPath("$[?(@.type == 'PERFORMER_RULES')].requiredAtRegistration").value(false));
  }

  @Test
  void registrationRejectsMissingOrOutdatedRequiredConsents() throws Exception {
    Map<String, Object> missingConsentPayload =
        new java.util.LinkedHashMap<>(registerPayload(newEmail()));
    missingConsentPayload.put(
        "consents",
        List.of(
            Map.of("type", "TERMS_OF_SERVICE", "documentVersion", "1.0"),
            Map.of("type", "PRIVACY_POLICY", "documentVersion", "1.0")));
    mockMvc
        .perform(
            withClientIp(post("/api/v1/auth/register"), newClientIp())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(missingConsentPayload)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("BAD_REQUEST"));

    Map<String, Object> outdatedPayload =
        new java.util.LinkedHashMap<>(registerPayload(newEmail()));
    outdatedPayload.put(
        "consents",
        List.of(
            Map.of("type", "TERMS_OF_SERVICE", "documentVersion", "0.9"),
            Map.of("type", "PRIVACY_POLICY", "documentVersion", "1.0"),
            Map.of("type", "PERSONAL_DATA_PROCESSING", "documentVersion", "1.0")));
    mockMvc
        .perform(
            withClientIp(post("/api/v1/auth/register"), newClientIp())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(outdatedPayload)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
        .andExpect(jsonPath("$.message").value("Outdated consent version for TERMS_OF_SERVICE"));
  }

  @Test
  void authenticatedUserCanAcceptCurrentConsentIdempotently() throws Exception {
    JsonNode registration = register(newEmail());
    String accessToken = registration.get("accessToken").asText();
    String body =
        objectMapper.writeValueAsString(
            List.of(Map.of("type", "PAYMENT_POLICY", "documentVersion", "1.0")));

    mockMvc
        .perform(
            post("/api/v1/users/me/consents")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header("X-Forwarded-For", "203.0.113.55")
                .header("User-Agent", "HandyNest-Test")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isOk());
    mockMvc
        .perform(
            post("/api/v1/users/me/consents")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isOk());

    Long userId = userId(registration);
    assertThat(userConsentRepository.findAllByUserIdOrderByAcceptedAtDesc(userId))
        .filteredOn(consent -> consent.getConsentType().name().equals("PAYMENT_POLICY"))
        .hasSize(1);
  }

  @Test
  void registerValidationErrorReturnsUnifiedBadRequestContract() throws Exception {
    mockMvc
        .perform(
            withClientIp(post("/api/v1/auth/register"), newClientIp())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of(
                            "firstName", "",
                            "lastName", "User",
                            "email", "not-an-email",
                            "password", "short",
                            "passwordConfirmation", "different"))))
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

    mockMvc
        .perform(
            withClientIp(post("/api/v1/auth/login"), newClientIp())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of(
                            "email", email,
                            "password", PASSWORD))))
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

    mockMvc
        .perform(get("/api/v1/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value(email))
        .andExpect(jsonPath("$.publicId").value(registration.get("user").get("publicId").asText()))
        .andExpect(jsonPath("$.id").doesNotExist());
  }

  @Test
  void refreshRotatesRefreshTokenAndRejectsOldToken() throws Exception {
    JsonNode registration = register(newEmail());
    String oldRefreshToken = registration.get("refreshToken").asText();

    JsonNode refreshed =
        objectMapper.readTree(
            mockMvc
                .perform(
                    withClientIp(post("/api/v1/auth/refresh"), newClientIp())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            objectMapper.writeValueAsString(
                                Map.of("refreshToken", oldRefreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.refreshToken").isString())
                .andReturn()
                .getResponse()
                .getContentAsString());

    assertThat(refreshed.get("refreshToken").asText()).isNotEqualTo(oldRefreshToken);

    mockMvc
        .perform(
            withClientIp(post("/api/v1/auth/refresh"), newClientIp())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("refreshToken", oldRefreshToken))))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
  }

  @Test
  void refreshCanRotateRefreshTokenFromHttpOnlyCookie() throws Exception {
    JsonNode registration = register(newEmail());
    String oldRefreshToken = registration.get("refreshToken").asText();

    JsonNode refreshed =
        objectMapper.readTree(
            mockMvc
                .perform(
                    withClientIp(post("/api/v1/auth/refresh"), newClientIp())
                        .cookie(refreshCookie(oldRefreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.refreshToken").isString())
                .andExpect(
                    header()
                        .string(
                            HttpHeaders.SET_COOKIE,
                            containsString(RefreshTokenCookieService.DEFAULT_COOKIE_NAME + "=")))
                .andReturn()
                .getResponse()
                .getContentAsString());

    assertThat(refreshed.get("refreshToken").asText()).isNotEqualTo(oldRefreshToken);

    mockMvc
        .perform(
            withClientIp(post("/api/v1/auth/refresh"), newClientIp())
                .cookie(refreshCookie(oldRefreshToken)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
  }

  @Test
  void logoutRevokesRefreshToken() throws Exception {
    JsonNode registration = register(newEmail());
    String accessToken = registration.get("accessToken").asText();
    String refreshToken = registration.get("refreshToken").asText();

    mockMvc
        .perform(
            withClientIp(post("/api/v1/auth/logout"), newClientIp())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
        .andExpect(status().isNoContent());

    mockMvc
        .perform(
            withClientIp(post("/api/v1/auth/refresh"), newClientIp())
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

    mockMvc
        .perform(
            withClientIp(post("/api/v1/auth/logout"), newClientIp())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .cookie(refreshCookie(refreshToken)))
        .andExpect(status().isNoContent())
        .andExpect(
            header()
                .string(
                    HttpHeaders.SET_COOKIE,
                    containsString(RefreshTokenCookieService.DEFAULT_COOKIE_NAME + "=")))
        .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")));

    mockMvc
        .perform(
            withClientIp(post("/api/v1/auth/refresh"), newClientIp())
                .cookie(refreshCookie(refreshToken)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
  }

  @Test
  void registerIsRateLimitedByClientIp() throws Exception {
    String clientIp = newClientIp();

    for (int i = 0; i < 3; i++) {
      mockMvc
          .perform(
              withClientIp(post("/api/v1/auth/register"), clientIp)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(registerPayload(newEmail()))))
          .andExpect(status().isCreated());
    }

    mockMvc
        .perform(
            withClientIp(post("/api/v1/auth/register"), clientIp)
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
      mockMvc
          .perform(
              withClientIp(post("/api/v1/auth/login"), clientIp)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      objectMapper.writeValueAsString(
                          Map.of("email", email, "password", "Wrong121314#"))))
          .andExpect(status().isUnauthorized());
    }

    mockMvc
        .perform(
            withClientIp(post("/api/v1/auth/login"), clientIp)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of("email", email, "password", "Wrong121314#"))))
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
      int status =
          mockMvc
              .perform(
                  withClientIp(post("/api/v1/auth/refresh"), clientIp)
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(
                          objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
              .andReturn()
              .getResponse()
              .getStatus();

      assertThat(status).isIn(200, 401);
    }

    mockMvc
        .perform(
            withClientIp(post("/api/v1/auth/refresh"), clientIp)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
        .andExpect(status().isTooManyRequests())
        .andExpect(header().exists(HttpHeaders.RETRY_AFTER))
        .andExpect(jsonPath("$.code").value("RATE_LIMITED"));
  }

  @Test
  void meWithoutTokenReturnsJsonUnauthorized() throws Exception {
    mockMvc
        .perform(get("/api/v1/auth/me"))
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
        .andExpect(jsonPath("$.message").value("Authentication required"));
  }

  @Test
  void meWithMalformedBearerTokenReturnsJsonUnauthorized() throws Exception {
    mockMvc
        .perform(get("/api/v1/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer malformed-token"))
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
  }

  @Test
  void publicCategoriesStayAvailableWithoutToken() throws Exception {
    mockMvc.perform(get("/api/v1/categories")).andExpect(status().isOk());
  }

  @Test
  void publicGeoEndpointsStayAvailableWithoutToken() throws Exception {
    mockMvc
        .perform(get("/api/v1/geo/countries"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].code").value("UZ"));
  }

  @Test
  void requestPhoneOtpRequiresAuthenticatedUserPhone() throws Exception {
    JsonNode registration = register(newEmail());

    mockMvc
        .perform(
            withClientIp(post("/api/v1/auth/request-phone-otp"), newClientIp())
                .header(HttpHeaders.AUTHORIZATION, bearer(registration)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
        .andExpect(jsonPath("$.message").value("Phone number is required"));
  }

  @Test
  void phoneOtpCanBeRequestedAndVerifiedWithoutPersistingRawCode() throws Exception {
    JsonNode registration = register(newEmail());
    String accessToken = registration.get("accessToken").asText();
    String phone = "+77001234567";
    updatePhone(accessToken, phone);

    mockMvc
        .perform(
            withClientIp(post("/api/v1/auth/request-phone-otp"), newClientIp())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.maskedPhone").value("****4567"))
        .andExpect(jsonPath("$.retryAfterSeconds").value(60))
        .andExpect(jsonPath("$.expiresAt").isString());

    String rawCode = smsProvider.lastCode(phone).orElseThrow();
    OtpCode storedCode = latestOtp(registration, phone);
    assertThat(storedCode.getCodeHash()).isNotEqualTo(rawCode);
    assertThat(storedCode.getCodeHash()).isEqualTo(hashService.sha256(rawCode));

    mockMvc
        .perform(
            withClientIp(post("/api/v1/auth/verify-phone-otp"), newClientIp())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("code", rawCode))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.phoneVerified").value(true));

    mockMvc
        .perform(get("/api/v1/users/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.phoneVerified").value(true));
  }

  @Test
  void phoneOtpVerificationPromotesExistingPerformerToPhoneVerifiedLevel() throws Exception {
    JsonNode registration = register(newEmail());
    String accessToken = registration.get("accessToken").asText();
    String phone = "+77005556677";

    mockMvc
        .perform(
            post("/api/v1/performers/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(performerPayload("Phone Verified Pro"))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.verificationLevel").value("NONE"))
        .andExpect(jsonPath("$.verificationStatus").value("NOT_SUBMITTED"));

    updatePhone(accessToken, phone);
    requestPhoneOtp(accessToken, phone);
    String rawCode = smsProvider.lastCode(phone).orElseThrow();

    mockMvc
        .perform(
            withClientIp(post("/api/v1/auth/verify-phone-otp"), newClientIp())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("code", rawCode))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.phoneVerified").value(true));

    mockMvc
        .perform(
            get("/api/v1/performers/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.verificationLevel").value("PHONE_VERIFIED"))
        .andExpect(jsonPath("$.verificationStatus").value("APPROVED"));
  }

  @Test
  void invalidPhoneOtpIncrementsAttemptsAndEventuallyBlocksVerification() throws Exception {
    JsonNode registration = register(newEmail());
    String accessToken = registration.get("accessToken").asText();
    String phone = "+77007654321";
    updatePhone(accessToken, phone);
    requestPhoneOtp(accessToken, phone);

    for (int i = 0; i < 5; i++) {
      mockMvc
          .perform(
              withClientIp(post("/api/v1/auth/verify-phone-otp"), newClientIp())
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(Map.of("code", "000000"))))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    assertThat(latestOtp(registration, phone).getAttempts()).isEqualTo(5);
    String rawCode = smsProvider.lastCode(phone).orElseThrow();

    mockMvc
        .perform(
            withClientIp(post("/api/v1/auth/verify-phone-otp"), newClientIp())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("code", rawCode))))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
  }

  @Test
  void expiredPhoneOtpCannotBeUsed() throws Exception {
    JsonNode registration = register(newEmail());
    String accessToken = registration.get("accessToken").asText();
    String phone = "+77002223344";
    updatePhone(accessToken, phone);
    requestPhoneOtp(accessToken, phone);
    String rawCode = smsProvider.lastCode(phone).orElseThrow();
    OtpCode storedCode = latestOtp(registration, phone);
    storedCode.setExpiresAt(Instant.now().minusSeconds(1));
    otpCodeRepository.saveAndFlush(storedCode);

    mockMvc
        .perform(
            withClientIp(post("/api/v1/auth/verify-phone-otp"), newClientIp())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("code", rawCode))))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
  }

  @Test
  void repeatedPhoneOtpRequestBeforeCooldownReturnsRateLimited() throws Exception {
    JsonNode registration = register(newEmail());
    String accessToken = registration.get("accessToken").asText();
    String phone = "+77008889900";
    String clientIp = newClientIp();
    updatePhone(accessToken, phone);

    mockMvc
        .perform(
            withClientIp(post("/api/v1/auth/request-phone-otp"), clientIp)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
        .andExpect(status().isOk());

    mockMvc
        .perform(
            withClientIp(post("/api/v1/auth/request-phone-otp"), clientIp)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
        .andExpect(status().isTooManyRequests())
        .andExpect(header().exists(HttpHeaders.RETRY_AFTER))
        .andExpect(jsonPath("$.code").value("RATE_LIMITED"));
  }

  @Test
  void verifyPhoneOtpIsRateLimitedByClientIpAndPhone() throws Exception {
    JsonNode registration = register(newEmail());
    String accessToken = registration.get("accessToken").asText();
    String phone = "+77001112233";
    String clientIp = newClientIp();
    updatePhone(accessToken, phone);
    requestPhoneOtp(accessToken, phone);

    for (int i = 0; i < 5; i++) {
      mockMvc
          .perform(
              withClientIp(post("/api/v1/auth/verify-phone-otp"), clientIp)
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(Map.of("code", "111111"))))
          .andExpect(status().isBadRequest());
    }

    mockMvc
        .perform(
            withClientIp(post("/api/v1/auth/verify-phone-otp"), clientIp)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("code", "111111"))))
        .andExpect(status().isTooManyRequests())
        .andExpect(header().exists(HttpHeaders.RETRY_AFTER))
        .andExpect(jsonPath("$.code").value("RATE_LIMITED"));
  }

  @Test
  void emailVerificationCanBeRequestedAndVerifiedWhenEnabled() throws Exception {
    JsonNode registration = register(newEmail());
    String accessToken = registration.get("accessToken").asText();
    String email = registration.get("user").get("email").asText();

    mockMvc
        .perform(
            withClientIp(post("/api/v1/auth/request-email-verification"), newClientIp())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.maskedEmail").isString())
        .andExpect(jsonPath("$.retryAfterSeconds").value(3600));

    String rawToken = emailProvider.lastToken(email).orElseThrow();
    EmailVerificationToken storedToken = latestEmailToken(registration, email);
    assertThat(storedToken.getTokenHash()).isNotEqualTo(rawToken);
    assertThat(storedToken.getTokenHash()).isEqualTo(hashService.sha256(rawToken));

    mockMvc
        .perform(
            withClientIp(post("/api/v1/auth/verify-email"), newClientIp())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("token", rawToken))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.emailVerified").value(true));

    mockMvc
        .perform(get("/api/v1/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.emailVerified").value(true));
  }

  @Test
  void emailVerificationTokenCannotBeReused() throws Exception {
    JsonNode registration = register(newEmail());
    String accessToken = registration.get("accessToken").asText();
    String email = registration.get("user").get("email").asText();

    mockMvc
        .perform(
            withClientIp(post("/api/v1/auth/request-email-verification"), newClientIp())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
        .andExpect(status().isOk());
    String rawToken = emailProvider.lastToken(email).orElseThrow();

    mockMvc
        .perform(
            withClientIp(post("/api/v1/auth/verify-email"), newClientIp())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("token", rawToken))))
        .andExpect(status().isOk());

    mockMvc
        .perform(
            withClientIp(post("/api/v1/auth/verify-email"), newClientIp())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("token", rawToken))))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
  }

  @Test
  void expiredEmailVerificationTokenCannotBeUsed() throws Exception {
    JsonNode registration = register(newEmail());
    String accessToken = registration.get("accessToken").asText();
    String email = registration.get("user").get("email").asText();

    mockMvc
        .perform(
            withClientIp(post("/api/v1/auth/request-email-verification"), newClientIp())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
        .andExpect(status().isOk());
    String rawToken = emailProvider.lastToken(email).orElseThrow();
    EmailVerificationToken storedToken = latestEmailToken(registration, email);
    storedToken.setExpiresAt(Instant.now().minusSeconds(1));
    emailVerificationTokenRepository.saveAndFlush(storedToken);

    mockMvc
        .perform(
            withClientIp(post("/api/v1/auth/verify-email"), newClientIp())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("token", rawToken))))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
  }

  private JsonNode register(String email) throws Exception {
    return register(email, newClientIp());
  }

  private JsonNode register(String email, String clientIp) throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                withClientIp(post("/api/v1/auth/register"), clientIp)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registerPayload(email))))
            .andExpect(status().isCreated())
            .andReturn();

    return objectMapper.readTree(result.getResponse().getContentAsString());
  }

  private void updatePhone(String accessToken, String phone) throws Exception {
    mockMvc
        .perform(
            patch("/api/v1/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("phone", phone))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.phone").value(phone))
        .andExpect(jsonPath("$.phoneVerified").value(false));
  }

  private void requestPhoneOtp(String accessToken, String phone) throws Exception {
    mockMvc
        .perform(
            withClientIp(post("/api/v1/auth/request-phone-otp"), newClientIp())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
        .andExpect(status().isOk());
    assertThat(smsProvider.lastCode(phone)).isPresent();
  }

  private OtpCode latestOtp(JsonNode registration, String phone) {
    return otpCodeRepository
        .findFirstByUserIdAndPhoneNumberAndPurposeAndConsumedAtIsNullOrderByCreatedAtDesc(
            userId(registration), phone, OtpPurpose.PHONE_VERIFICATION)
        .orElseThrow();
  }

  private EmailVerificationToken latestEmailToken(JsonNode registration, String email) {
    return emailVerificationTokenRepository
        .findFirstByUserIdAndEmailAndConsumedAtIsNullOrderByCreatedAtDesc(
            userId(registration), email)
        .orElseThrow();
  }

  private Long userId(JsonNode registration) {
    String publicId = registration.get("user").get("publicId").asText();
    return userRepository.findByPublicId(publicId).orElseThrow().getId();
  }

  private Map<String, Object> registerPayload(String email) {
    return AuthTestPayloads.registrationPayload("Test", "User", email, PASSWORD);
  }

  private Map<String, Object> performerPayload(String displayName) {
    return new java.util.LinkedHashMap<>(
        Map.of(
            "displayName",
            displayName,
            "description",
            "Профиль исполнителя для auth verification теста",
            "skillsDescription",
            "Ремонт и диагностика",
            "cityId",
            TASHKENT_CITY_PUBLIC_ID,
            "serviceRadiusKm",
            30,
            "worksRemotely",
            false,
            "worksOnsite",
            true,
            "categories",
            java.util.List.of(
                Map.of(
                    "categoryId",
                    REPAIR_CATEGORY_PUBLIC_ID,
                    "experienceYears",
                    3,
                    "priceFrom",
                    5000,
                    "priceTo",
                    20000,
                    "currency",
                    "UZS",
                    "primary",
                    true))));
  }

  private String newEmail() {
    return "auth" + UUID.randomUUID().toString().replace("-", "").substring(0, 16) + "@e.kz";
  }

  private Cookie refreshCookie(String refreshToken) {
    return new Cookie(RefreshTokenCookieService.DEFAULT_COOKIE_NAME, refreshToken);
  }

  private String bearer(JsonNode registration) {
    return "Bearer " + registration.get("accessToken").asText();
  }

  private MockHttpServletRequestBuilder withClientIp(
      MockHttpServletRequestBuilder requestBuilder, String clientIp) {
    return requestBuilder.header("X-Forwarded-For", clientIp);
  }

  private String newClientIp() {
    int value = CLIENT_IP_SEQUENCE.getAndIncrement();
    return "10.0." + (value / 250) + "." + ((value % 250) + 1);
  }

  @TestConfiguration
  static class VerificationProviderTestConfig {

    @Bean
    @Primary
    CapturingSmsProvider capturingSmsProvider() {
      return new CapturingSmsProvider();
    }

    @Bean
    @Primary
    CapturingEmailProvider capturingEmailProvider() {
      return new CapturingEmailProvider();
    }
  }

  static class CapturingSmsProvider implements SmsProvider {

    private final ConcurrentMap<String, String> codesByPhone = new ConcurrentHashMap<>();

    @Override
    public void sendOtp(String phoneNumber, String otpCode) {
      codesByPhone.put(phoneNumber, otpCode);
    }

    Optional<String> lastCode(String phoneNumber) {
      return Optional.ofNullable(codesByPhone.get(phoneNumber));
    }
  }

  static class CapturingEmailProvider implements EmailProvider {

    private final ConcurrentMap<String, String> tokensByEmail = new ConcurrentHashMap<>();

    @Override
    public void sendVerificationToken(String email, String token) {
      tokensByEmail.put(email, token);
    }

    Optional<String> lastToken(String email) {
      return Optional.ofNullable(tokensByEmail.get(email));
    }
  }
}
