package com.handynest.common.openapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.handynest.HandyNestProjectApplication;
import com.handynest.testsupport.TestDatabaseConfig;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(classes = {TestDatabaseConfig.class, HandyNestProjectApplication.class})
class OpenApiContractTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void publicOpenApiContractContainsOnlyApiV1PathsAndExpectedSecurityShape() throws Exception {
    JsonNode spec = openApiSpec();
    JsonNode paths = spec.path("paths");

    assertThat(paths.has("/api/v1/auth/register")).isTrue();
    assertThat(paths.has("/api/v1/auth/request-phone-otp")).isTrue();
    assertThat(paths.has("/api/v1/auth/verify-phone-otp")).isTrue();
    assertThat(paths.has("/api/v1/auth/request-email-verification")).isTrue();
    assertThat(paths.has("/api/v1/auth/verify-email")).isTrue();
    assertThat(paths.has("/api/v1/users/me")).isTrue();
    assertThat(paths.has("/api/v1/market/config")).isTrue();
    assertThat(paths.has("/api/v1/legal/consent-requirements")).isTrue();
    assertThat(paths.has("/api/v1/users/me/consents")).isTrue();
    assertThat(paths.has("/api/v1/complaints")).isTrue();
    assertThat(paths.has("/api/v1/admin/moderation-cases/{caseId}/start-review")).isTrue();
    assertThat(paths.has("/api/v1/admin/disputes/{disputeId}/request-customer-evidence")).isTrue();
    assertThat(paths.has("/api/v1/tasks")).isTrue();
    assertThat(paths.has("/api/v1/chats/{chatId}/submit-work")).isTrue();
    assertThat(paths.has("/api/v1/payments/{paymentId}/authorize")).isTrue();
    assertThat(paths.has("/api/v1/verification/documents/upload-url")).isTrue();
    assertThat(paths.has("/api/v1/performers/me/verification-requests")).isTrue();
    assertThat(
            paths.has(
                "/api/v1/admin/verification-requests/{requestId}/documents/{documentId}/download-url"))
        .isTrue();
    assertThat(paths.has("/api/v1/admin/performer-categories")).isTrue();
    assertThat(paths.has("/api/v1/admin/performers/{performerId}/categories/{categoryId}/approve"))
        .isTrue();
    assertThat(paths.has("/api/v1/admin/categories")).isTrue();
    assertThat(paths.has("/api/v1/admin/categories/{categoryId}")).isTrue();
    assertThat(paths.has("/api/v1/admin/categories/{categoryId}/audit-events")).isTrue();
    assertThat(paths.has("/users")).isFalse();
    assertThat(paths.has("/tasks")).isFalse();
    assertThat(paths.has("/feedbacks")).isFalse();

    Iterator<String> pathNames = paths.fieldNames();
    while (pathNames.hasNext()) {
      assertThat(pathNames.next()).startsWith("/api/v1/");
    }

    JsonNode bearerAuth = spec.path("components").path("securitySchemes").path("bearerAuth");
    assertThat(bearerAuth.path("type").asText()).isEqualTo("http");
    assertThat(bearerAuth.path("scheme").asText()).isEqualTo("bearer");

    assertThat(paths.path("/api/v1/auth/register").path("post").path("security")).isEmpty();
    assertThat(paths.path("/api/v1/market/config").path("get").path("security")).isEmpty();
    assertThat(paths.path("/api/v1/auth/verify-email").path("post").path("security")).isEmpty();
    assertThat(paths.path("/api/v1/legal/consent-requirements").path("get").path("security"))
        .isEmpty();
    assertThat(
            paths
                .path("/api/v1/auth/request-phone-otp")
                .path("post")
                .path("security")
                .get(0)
                .has("bearerAuth"))
        .isTrue();
    assertThat(
            paths
                .path("/api/v1/users/me/consents")
                .path("post")
                .path("security")
                .get(0)
                .has("bearerAuth"))
        .isTrue();
    assertThat(
            paths
                .path("/api/v1/auth/verify-phone-otp")
                .path("post")
                .path("security")
                .get(0)
                .has("bearerAuth"))
        .isTrue();
    assertThat(
            paths
                .path("/api/v1/auth/request-email-verification")
                .path("post")
                .path("security")
                .get(0)
                .has("bearerAuth"))
        .isTrue();
    assertThat(paths.path("/api/v1/payments/webhooks/{provider}").path("post").path("security"))
        .isEmpty();
    assertThat(
            paths.path("/api/v1/users/me").path("delete").path("security").get(0).has("bearerAuth"))
        .isTrue();
    assertThat(
            paths
                .path("/api/v1/verification/documents/upload-url")
                .path("post")
                .path("security")
                .get(0)
                .has("bearerAuth"))
        .isTrue();
    assertThat(
            paths
                .path("/api/v1/admin/performer-categories")
                .path("get")
                .path("security")
                .get(0)
                .has("bearerAuth"))
        .isTrue();
    assertThat(
            paths
                .path("/api/v1/admin/categories")
                .path("post")
                .path("security")
                .get(0)
                .has("bearerAuth"))
        .isTrue();
  }

  @Test
  void openApiDocumentsMarketplaceCorePaths() throws Exception {
    JsonNode paths = openApiSpec().path("paths");

    List<String> corePaths =
        List.of(
            "/api/v1/tasks",
            "/api/v1/tasks/{taskId}",
            "/api/v1/tasks/{taskId}/repeat",
            "/api/v1/tasks/{taskId}/cancel",
            "/api/v1/my/tasks",
            "/api/v1/tasks/{taskId}/offers",
            "/api/v1/tasks/{taskId}/offers/{offerId}/accept",
            "/api/v1/tasks/{taskId}/offers/{offerId}/cancel",
            "/api/v1/tasks/{taskId}/attachments",
            "/api/v1/tasks/{taskId}/attachments/{attachmentId}/download-url",
            "/api/v1/my/offers",
            "/api/v1/deals/{dealId}",
            "/api/v1/deals/{dealId}/contact-reveals",
            "/api/v1/deals/{dealId}/cancel",
            "/api/v1/my/deals",
            "/api/v1/chats",
            "/api/v1/chats/{chatId}",
            "/api/v1/chats/{chatId}/messages",
            "/api/v1/chats/{chatId}/messages/timeline",
            "/api/v1/chats/{chatId}/attachments/{attachmentId}/complete",
            "/api/v1/chats/{chatId}/submit-work",
            "/api/v1/chats/{chatId}/accept-work",
            "/api/v1/chats/{chatId}/request-revision",
            "/api/v1/chats/{chatId}/open-dispute",
            "/api/v1/tasks/{taskId}/feedbacks",
            "/api/v1/users/{userId}/feedbacks",
            "/api/v1/performers/{performerId}/feedbacks",
            "/api/v1/disputes/{disputeId}",
            "/api/v1/my/disputes",
            "/api/v1/my/favorite-performers",
            "/api/v1/my/favorite-performers/{performerId}",
            "/api/v1/admin/disputes",
            "/api/v1/admin/disputes/{disputeId}/start-review",
            "/api/v1/admin/disputes/{disputeId}/request-customer-evidence",
            "/api/v1/admin/disputes/{disputeId}/request-performer-evidence",
            "/api/v1/admin/disputes/{disputeId}/resolve",
            "/api/v1/complaints",
            "/api/v1/deals/{dealId}/contact-reveals",
            "/api/v1/deals/{dealId}/cancel",
            "/api/v1/my/complaints",
            "/api/v1/complaints/{complaintId}/cancel",
            "/api/v1/admin/moderation-cases",
            "/api/v1/admin/moderation-cases/{caseId}/start-review",
            "/api/v1/admin/moderation-cases/{caseId}/cancel",
            "/api/v1/chats/{chatId}/attachments",
            "/api/v1/disputes/{disputeId}/attachments",
            "/api/v1/attachments/{attachmentId}/download-url",
            "/api/v1/deals/{dealId}/milestones",
            "/api/v1/milestones/{milestoneId}",
            "/api/v1/admin/tasks/{taskId}/publish",
            "/api/v1/admin/tasks/{taskId}/reject");

    for (String path : corePaths) {
      assertThat(paths.has(path)).as("OpenAPI should document marketplace path %s", path).isTrue();
    }
  }

  @Test
  void openApiDocumentsStandardErrorsAndRequiredIdempotencyHeaders() throws Exception {
    JsonNode spec = openApiSpec();
    JsonNode paths = spec.path("paths");

    JsonNode createTask = operation(paths, "/api/v1/tasks", "post");
    assertThat(hasRequiredIdempotencyHeader(createTask)).isTrue();
    assertThat(createTask.path("responses").has("400")).isTrue();
    assertThat(createTask.path("responses").has("401")).isTrue();
    assertThat(createTask.path("responses").has("403")).isTrue();
    assertThat(createTask.path("responses").has("429")).isTrue();

    JsonNode webhook = operation(paths, "/api/v1/payments/webhooks/{provider}", "post");
    assertThat(hasRequiredIdempotencyHeader(webhook)).isTrue();
    assertThat(hasHeaderParameter(webhook, "X-HandyNest-Webhook-Token")).isTrue();

    List<String> criticalMarketplacePosts =
        List.of(
            "/api/v1/tasks",
            "/api/v1/tasks/{taskId}/repeat",
            "/api/v1/tasks/{taskId}/offers",
            "/api/v1/tasks/{taskId}/offers/{offerId}/accept",
            "/api/v1/tasks/{taskId}/offers/{offerId}/cancel",
            "/api/v1/chats/{chatId}/submit-work",
            "/api/v1/chats/{chatId}/accept-work",
            "/api/v1/chats/{chatId}/request-revision",
            "/api/v1/chats/{chatId}/open-dispute",
            "/api/v1/tasks/{taskId}/feedbacks",
            "/api/v1/deals/{dealId}/milestones",
            "/api/v1/milestones/{milestoneId}/start",
            "/api/v1/milestones/{milestoneId}/submit",
            "/api/v1/milestones/{milestoneId}/accept",
            "/api/v1/milestones/{milestoneId}/reject",
            "/api/v1/milestones/{milestoneId}/dispute",
            "/api/v1/milestones/{milestoneId}/cancel",
            "/api/v1/complaints",
            "/api/v1/disputes/{disputeId}/attachments",
            "/api/v1/admin/disputes/{disputeId}/resolve");

    for (String path : criticalMarketplacePosts) {
      assertThat(hasRequiredIdempotencyHeader(operation(paths, path, "post")))
          .as("POST %s must require Idempotency-Key", path)
          .isTrue();
    }

    JsonNode publicCategories = operation(paths, "/api/v1/categories", "get");
    assertThat(publicCategories.path("security")).isEmpty();
    assertThat(publicCategories.path("responses").has("401")).isFalse();
    assertThat(hasRequiredIdempotencyHeader(operation(paths, "/api/v1/admin/categories", "post")))
        .isTrue();
  }

  @Test
  void marketplacePublicAndPrivateOperationsHaveExpectedSecurityShape() throws Exception {
    JsonNode paths = openApiSpec().path("paths");

    List<String> publicGets =
        List.of(
            "/api/v1/tasks",
            "/api/v1/tasks/{taskId}",
            "/api/v1/tasks/{taskId}/attachments",
            "/api/v1/tasks/{taskId}/attachments/{attachmentId}/download-url",
            "/api/v1/tasks/{taskId}/feedbacks",
            "/api/v1/users/{userId}/feedbacks",
            "/api/v1/performers/{performerId}/feedbacks");
    for (String path : publicGets) {
      assertThat(hasEmptySecurity(operation(paths, path, "get")))
          .as("GET %s should be public in OpenAPI", path)
          .isTrue();
    }

    List<OperationRef> privateOperations =
        List.of(
            new OperationRef("/api/v1/tasks", "post"),
            new OperationRef("/api/v1/tasks/{taskId}/repeat", "post"),
            new OperationRef("/api/v1/tasks/{taskId}/cancel", "post"),
            new OperationRef("/api/v1/my/tasks", "get"),
            new OperationRef("/api/v1/tasks/{taskId}/offers", "get"),
            new OperationRef("/api/v1/tasks/{taskId}/offers", "post"),
            new OperationRef("/api/v1/tasks/{taskId}/offers/{offerId}/accept", "post"),
            new OperationRef("/api/v1/tasks/{taskId}/offers/{offerId}/cancel", "post"),
            new OperationRef("/api/v1/tasks/{taskId}/attachments", "post"),
            new OperationRef("/api/v1/my/offers", "get"),
            new OperationRef("/api/v1/deals/{dealId}", "get"),
            new OperationRef("/api/v1/deals/{dealId}/contact-reveals", "get"),
            new OperationRef("/api/v1/deals/{dealId}/contact-reveals", "post"),
            new OperationRef("/api/v1/deals/{dealId}/cancel", "post"),
            new OperationRef("/api/v1/my/deals", "get"),
            new OperationRef("/api/v1/chats", "get"),
            new OperationRef("/api/v1/chats/{chatId}", "get"),
            new OperationRef("/api/v1/chats/{chatId}/messages", "get"),
            new OperationRef("/api/v1/chats/{chatId}/messages", "post"),
            new OperationRef("/api/v1/chats/{chatId}/messages/timeline", "get"),
            new OperationRef("/api/v1/chats/{chatId}/attachments/{attachmentId}/complete", "post"),
            new OperationRef("/api/v1/chats/{chatId}/submit-work", "post"),
            new OperationRef("/api/v1/admin/tasks/{taskId}/publish", "post"));

    for (OperationRef ref : privateOperations) {
      assertThat(requiresBearer(operation(paths, ref.path(), ref.method())))
          .as("%s %s should require bearerAuth in OpenAPI", ref.method().toUpperCase(), ref.path())
          .isTrue();
    }
  }

  @Test
  void marketplaceOperationIdsArePresentAndUnique() throws Exception {
    JsonNode paths = openApiSpec().path("paths");
    Set<String> seenOperationIds = new HashSet<>();

    Iterator<String> pathNames = paths.fieldNames();
    while (pathNames.hasNext()) {
      String path = pathNames.next();
      if (!isMarketplacePath(path)) {
        continue;
      }

      Iterator<String> methods = paths.path(path).fieldNames();
      while (methods.hasNext()) {
        String method = methods.next();
        JsonNode operation = paths.path(path).path(method);
        String operationId = operation.path("operationId").asText();
        assertThat(operationId)
            .as("%s %s should have operationId", method.toUpperCase(), path)
            .isNotBlank();
        assertThat(seenOperationIds.add(operationId))
            .as("operationId %s should be unique", operationId)
            .isTrue();
      }
    }
  }

  @Test
  void marketplaceOperationsUseFrontendReadyTags() throws Exception {
    JsonNode paths = openApiSpec().path("paths");

    assertThat(firstTag(operation(paths, "/api/v1/tasks", "post"))).isEqualTo("Marketplace Tasks");
    assertThat(firstTag(operation(paths, "/api/v1/tasks/{taskId}/offers", "post")))
        .isEqualTo("Task Offers");
    assertThat(firstTag(operation(paths, "/api/v1/deals/{dealId}", "get"))).isEqualTo("Deals");
    assertThat(firstTag(operation(paths, "/api/v1/chats/{chatId}", "get"))).isEqualTo("Chats");
    assertThat(firstTag(operation(paths, "/api/v1/tasks/{taskId}/feedbacks", "get")))
        .isEqualTo("Feedback");
    assertThat(firstTag(operation(paths, "/api/v1/my/favorite-performers", "get")))
        .isEqualTo("Marketplace Favorites");
    assertThat(firstTag(operation(paths, "/api/v1/admin/tasks/{taskId}/publish", "post")))
        .isEqualTo("Admin Marketplace");
    assertThat(firstTag(operation(paths, "/api/v1/performers/me/verification-requests", "post")))
        .isEqualTo("Verification");
    assertThat(firstTag(operation(paths, "/api/v1/admin/verification-requests", "get")))
        .isEqualTo("Admin Verification");
    assertThat(firstTag(operation(paths, "/api/v1/admin/performer-categories", "get")))
        .isEqualTo("Admin Verification");
    assertThat(firstTag(operation(paths, "/api/v1/admin/categories", "post")))
        .isEqualTo("Admin Categories");
  }

  @Test
  void keyFrontendDtosDoNotExposeNumericDatabaseIds() throws Exception {
    JsonNode schemas = openApiSpec().path("components").path("schemas");

    List<String> schemaNames =
        List.of(
            "AuthUserResponse",
            "UserProfileResponse",
            "PerformerProfileResponse",
            "MarketplaceTaskResponse",
            "TaskOfferResponse",
            "DealResponse",
            "ContactRevealResponse",
            "ContactRevealHistoryResponse",
            "DealChatResponse",
            "ChatMessageResponse",
            "FeedbackResponse",
            "DisputeCaseResponse",
            "AttachmentResponse",
            "AttachmentUploadResponse",
            "AttachmentDownloadUrlResponse",
            "TaskAttachmentResponse",
            "TaskAttachmentUploadResponse",
            "MilestoneResponse",
            "FavoritePerformerResponse",
            "PaymentTransactionResponse",
            "VerificationRequestResponse",
            "VerificationDocumentUploadRequest",
            "PerformerCategoryModerationResponse",
            "CategoryAdminResponse",
            "NotificationResponse");

    for (String schemaName : schemaNames) {
      JsonNode schema = schemas.path(schemaName);
      assertThat(schema.isMissingNode())
          .as("schema %s should exist in OpenAPI components", schemaName)
          .isFalse();
      assertThat(schema.path("properties").has("id"))
          .as("schema %s must not expose numeric database id", schemaName)
          .isFalse();
    }

    JsonNode attachmentProperties = schemas.path("AttachmentResponse").path("properties");
    assertThat(attachmentProperties.has("bucket")).isFalse();
    assertThat(attachmentProperties.has("storageKey")).isFalse();
    assertThat(attachmentProperties.has("storageProvider")).isFalse();
    assertThat(attachmentProperties.has("visibility")).isFalse();
    assertThat(schemas.has("ChatMessageTimelineResponse")).isTrue();
    assertThat(schemas.has("ChatAttachmentCompleteResponse")).isTrue();
    assertThat(schemas.path("ContactRevealHistoryResponse").path("properties").has("contactValue"))
        .isFalse();
  }

  private JsonNode openApiSpec() throws Exception {
    String body =
        mockMvc
            .perform(get("/v3/api-docs"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andReturn()
            .getResponse()
            .getContentAsString();
    return objectMapper.readTree(body);
  }

  private JsonNode operation(JsonNode paths, String path, String method) {
    JsonNode operation = paths.path(path).path(method);
    assertThat(operation.isMissingNode())
        .as("OpenAPI operation %s %s should exist", method.toUpperCase(), path)
        .isFalse();
    return operation;
  }

  private boolean hasHeaderParameter(JsonNode operation, String name) {
    for (JsonNode parameter : operation.path("parameters")) {
      if ("header".equals(parameter.path("in").asText())
          && name.equals(parameter.path("name").asText())) {
        return true;
      }
    }
    return false;
  }

  private boolean hasRequiredIdempotencyHeader(JsonNode operation) {
    for (JsonNode parameter : operation.path("parameters")) {
      if ("header".equals(parameter.path("in").asText())
          && "Idempotency-Key".equals(parameter.path("name").asText())) {
        return parameter.path("required").asBoolean()
            && "string".equals(parameter.path("schema").path("type").asText())
            && parameter.path("schema").path("maxLength").asInt() == 120;
      }
    }
    return false;
  }

  private boolean hasEmptySecurity(JsonNode operation) {
    return operation.path("security").isArray() && operation.path("security").isEmpty();
  }

  private boolean requiresBearer(JsonNode operation) {
    for (JsonNode requirement : operation.path("security")) {
      if (requirement.has("bearerAuth")) {
        return true;
      }
    }
    return false;
  }

  private String firstTag(JsonNode operation) {
    return operation.path("tags").path(0).asText();
  }

  private boolean isMarketplacePath(String path) {
    return path.startsWith("/api/v1/tasks")
        || path.startsWith("/api/v1/deals")
        || path.startsWith("/api/v1/milestones")
        || path.startsWith("/api/v1/chats")
        || path.startsWith("/api/v1/disputes")
        || path.startsWith("/api/v1/attachments")
        || path.startsWith("/api/v1/my/tasks")
        || path.startsWith("/api/v1/my/offers")
        || path.startsWith("/api/v1/my/deals")
        || path.startsWith("/api/v1/my/disputes")
        || path.startsWith("/api/v1/my/favorite-performers")
        || path.startsWith("/api/v1/admin/tasks")
        || path.startsWith("/api/v1/admin/disputes")
        || path.startsWith("/api/v1/verification")
        || path.contains("/verification-requests")
        || path.startsWith("/api/v1/admin/performer-categories")
        || path.startsWith("/api/v1/admin/categories")
        || (path.startsWith("/api/v1/admin/performers") && path.contains("/categories/"))
        || path.matches("^/api/v1/users/\\{[^/]+}/feedbacks$")
        || path.matches("^/api/v1/performers/\\{[^/]+}/feedbacks$");
  }

  private record OperationRef(String path, String method) {}
}
