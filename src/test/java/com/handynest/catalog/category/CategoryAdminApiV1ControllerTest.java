package com.handynest.catalog.category;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.handynest.HandyNestProjectApplication;
import com.handynest.identity.RoleName;
import com.handynest.identity.User;
import com.handynest.identity.UserRepository;
import com.handynest.testsupport.AuthTestPayloads;
import com.handynest.testsupport.TestDatabaseConfig;
import java.util.LinkedHashMap;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(classes = {TestDatabaseConfig.class, HandyNestProjectApplication.class})
class CategoryAdminApiV1ControllerTest {

  private static final String PASSWORD = "Test121314#";
  private static final String TASHKENT_CITY_PUBLIC_ID = "06UZCT00000000000000000001";
  private static final AtomicInteger CLIENT_IP_SEQUENCE = new AtomicInteger(20);

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private UserRepository userRepository;

  @Test
  void categoryAdminEndpointsRequireAdminRole() throws Exception {
    mockMvc.perform(get("/api/v1/admin/categories")).andExpect(status().isUnauthorized());

    String userToken = register(newEmail("user"));
    mockMvc
        .perform(
            get("/api/v1/admin/categories").header(HttpHeaders.AUTHORIZATION, bearer(userToken)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
  }

  @Test
  void adminCanCreateSearchHideAndAuditCategory() throws Exception {
    String adminToken = adminToken();
    String slug = "window-repair-" + randomSuffix();
    Map<String, Object> createPayload = categoryPayload(slug, true, null);
    String idempotencyKey = "category-" + UUID.randomUUID();

    MvcResult createResult =
        mockMvc
            .perform(
                post("/api/v1/admin/categories")
                    .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                    .header("Idempotency-Key", idempotencyKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createPayload)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.publicId").isString())
            .andExpect(jsonPath("$.slug").value(slug))
            .andExpect(jsonPath("$.translations[*].locale", hasItem("ru")))
            .andExpect(jsonPath("$.translations[*].locale", hasItem("kk")))
            .andExpect(jsonPath("$.synonyms[*].value", hasItem("ремонт стеклопакета " + slug)))
            .andReturn();
    JsonNode created = objectMapper.readTree(createResult.getResponse().getContentAsString());
    String categoryId = created.get("publicId").asText();
    long version = created.get("version").asLong();

    mockMvc
        .perform(
            post("/api/v1/admin/categories")
                .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                .header("Idempotency-Key", idempotencyKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createPayload)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.publicId").value(categoryId));

    mockMvc
        .perform(get("/api/v1/categories").queryParam("query", "стеклопакета " + slug))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].publicId").value(categoryId));

    Map<String, Object> hiddenPayload = categoryPayload(slug, false, version);
    mockMvc
        .perform(
            put("/api/v1/admin/categories/{categoryId}", categoryId)
                .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(hiddenPayload)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.publicVisible").value(false))
        .andExpect(jsonPath("$.version").value(version + 1));

    mockMvc
        .perform(get("/api/v1/categories/{categoryId}", categoryId))
        .andExpect(status().isNotFound());

    mockMvc
        .perform(
            put("/api/v1/admin/categories/{categoryId}", categoryId)
                .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(hiddenPayload)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("CONFLICT"));

    mockMvc
        .perform(
            get("/api/v1/admin/categories/{categoryId}/audit-events", categoryId)
                .header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].action").value("UPDATED"))
        .andExpect(jsonPath("$[0].changedFields").value("publicVisible"))
        .andExpect(jsonPath("$[1].action").value("CREATED"));
  }

  @Test
  void unsafeRiskConfigurationIsRejected() throws Exception {
    String adminToken = adminToken();
    Map<String, Object> payload = categoryPayload("unsafe-" + randomSuffix(), true, null);
    payload.put("riskLevel", "HIGH");
    payload.put("requiresVerificationLevel", "NONE");

    mockMvc
        .perform(
            post("/api/v1/admin/categories")
                .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                .header("Idempotency-Key", "category-" + UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
  }

  @Test
  void enablingManualApprovalMovesExistingPerformerAssignmentToPending() throws Exception {
    String adminToken = adminToken();
    String slug = "licensed-service-" + randomSuffix();
    Map<String, Object> createPayload = categoryPayload(slug, false, null);
    MvcResult createResult =
        mockMvc
            .perform(
                post("/api/v1/admin/categories")
                    .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                    .header("Idempotency-Key", "category-" + UUID.randomUUID())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createPayload)))
            .andExpect(status().isCreated())
            .andReturn();
    JsonNode category = objectMapper.readTree(createResult.getResponse().getContentAsString());
    String categoryId = category.get("publicId").asText();

    String performerToken = register(newEmail("performer"));
    mockMvc
        .perform(
            post("/api/v1/performers/me")
                .header(HttpHeaders.AUTHORIZATION, bearer(performerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(performerPayload(categoryId))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.categories[0].approvalStatus").value("NOT_REQUIRED"));

    Map<String, Object> updatePayload =
        categoryPayload(slug, false, category.get("version").asLong());
    updatePayload.put("requiresManualApproval", true);
    mockMvc
        .perform(
            put("/api/v1/admin/categories/{categoryId}", categoryId)
                .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatePayload)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.requiresManualApproval").value(true));

    mockMvc
        .perform(
            get("/api/v1/performers/me").header(HttpHeaders.AUTHORIZATION, bearer(performerToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.categories[0].categoryId").value(categoryId))
        .andExpect(jsonPath("$.categories[0].approvalStatus").value("PENDING"));
  }

  private Map<String, Object> categoryPayload(String slug, boolean publicVisible, Long version) {
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("slug", slug);
    payload.put("parentId", null);
    payload.put("icon", "wrench");
    payload.put("riskLevel", "LOW");
    payload.put("serviceMode", "HYBRID");
    payload.put("launchPhase", "MVP");
    payload.put("active", true);
    payload.put("publicVisible", publicVisible);
    payload.put("sortOrder", 999);
    payload.put("requiresVerificationLevel", "NONE");
    payload.put("requiresManualApproval", false);
    payload.put("requiresLicense", false);
    payload.put("allowsRemote", true);
    payload.put("allowsOnsite", true);
    payload.put("allowsEscrow", true);
    payload.put("allowsCash", true);
    payload.put("allowsMilestones", false);
    payload.put("allowsAttachments", true);
    payload.put("allowsContactReveal", false);
    payload.put("requiresOnsiteCoordination", false);
    payload.put("contactRevealStage", "NEVER");
    payload.put(
        "translations",
        List.of(
            Map.of(
                "locale",
                "ru",
                "name",
                "Ремонт окон",
                "description",
                "Ремонт окон и стеклопакетов"),
            Map.of("locale", "kk", "name", "Терезе жөндеу", "description", "Терезелерді жөндеу")));
    payload.put(
        "synonyms",
        List.of(
            Map.of("locale", "ru", "value", "ремонт стеклопакета " + slug),
            Map.of("locale", "kk", "value", "терезе шебері")));
    if (version != null) {
      payload.put("version", version);
    }
    return payload;
  }

  private Map<String, Object> performerPayload(String categoryId) {
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("displayName", "Category specialist");
    payload.put("description", "Performer used to verify category policy reconciliation");
    payload.put("skillsDescription", "Category policy integration test");
    payload.put("cityId", TASHKENT_CITY_PUBLIC_ID);
    payload.put("serviceRadiusKm", 20);
    payload.put("worksRemotely", true);
    payload.put("worksOnsite", true);
    payload.put("travelFeePolicy", "included");
    payload.put(
        "categories",
        List.of(
            Map.of(
                "categoryId",
                categoryId,
                "experienceYears",
                3,
                "priceFrom",
                1000,
                "priceTo",
                5000,
                "currency",
                "UZS",
                "primary",
                true)));
    return payload;
  }

  private String adminToken() throws Exception {
    String email = newEmail("admin");
    String token = register(email);
    User user = userRepository.findByEmail(email).orElseThrow();
    user.getRoles().add(RoleName.ADMIN);
    userRepository.saveAndFlush(user);
    return token;
  }

  private String register(String email) throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                post("/api/v1/auth/register")
                    .header("X-Forwarded-For", "198.51.100." + CLIENT_IP_SEQUENCE.getAndIncrement())
                    .header("Idempotency-Key", "register-" + UUID.randomUUID())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            AuthTestPayloads.registrationPayload(
                                "Category", "Admin", email, PASSWORD))))
            .andExpect(status().isCreated())
            .andReturn();
    return objectMapper
        .readTree(result.getResponse().getContentAsString())
        .get("accessToken")
        .asText();
  }

  private String bearer(String token) {
    return "Bearer " + token;
  }

  private String newEmail(String prefix) {
    return prefix + randomSuffix() + "@category.test";
  }

  private String randomSuffix() {
    return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
  }
}
