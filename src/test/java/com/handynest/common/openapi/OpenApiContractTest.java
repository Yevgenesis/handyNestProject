package com.handynest.common.openapi;

import com.handynest.HandyNestProjectApplication;
import com.handynest.testsupport.TestDatabaseConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(classes = {TestDatabaseConfig.class, HandyNestProjectApplication.class})
class OpenApiContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void publicOpenApiContractContainsOnlyApiV1PathsAndExpectedSecurityShape() throws Exception {
        JsonNode spec = openApiSpec();
        JsonNode paths = spec.path("paths");

        assertThat(paths.has("/api/v1/auth/register")).isTrue();
        assertThat(paths.has("/api/v1/users/me")).isTrue();
        assertThat(paths.has("/api/v1/tasks")).isTrue();
        assertThat(paths.has("/api/v1/chats/{chatId}/submit-work")).isTrue();
        assertThat(paths.has("/api/v1/payments/{paymentId}/authorize")).isTrue();
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
        assertThat(paths.path("/api/v1/payments/webhooks/{provider}").path("post").path("security")).isEmpty();
        assertThat(paths.path("/api/v1/users/me").path("delete")
                .path("security").get(0).has("bearerAuth")).isTrue();
    }

    @Test
    void openApiDocumentsStandardErrorsAndIdempotencyHeaders() throws Exception {
        JsonNode spec = openApiSpec();

        JsonNode createTask = spec.path("paths").path("/api/v1/tasks").path("post");
        assertThat(hasHeaderParameter(createTask, "Idempotency-Key")).isTrue();
        assertThat(createTask.path("responses").has("400")).isTrue();
        assertThat(createTask.path("responses").has("401")).isTrue();
        assertThat(createTask.path("responses").has("403")).isTrue();
        assertThat(createTask.path("responses").has("429")).isTrue();

        JsonNode webhook = spec.path("paths").path("/api/v1/payments/webhooks/{provider}").path("post");
        assertThat(hasHeaderParameter(webhook, "Idempotency-Key")).isTrue();
        assertThat(hasHeaderParameter(webhook, "X-HandyNest-Webhook-Token")).isTrue();

        JsonNode publicCategories = spec.path("paths").path("/api/v1/categories").path("get");
        assertThat(publicCategories.path("security")).isEmpty();
        assertThat(publicCategories.path("responses").has("401")).isFalse();
    }

    @Test
    void keyFrontendDtosDoNotExposeNumericDatabaseIds() throws Exception {
        JsonNode schemas = openApiSpec().path("components").path("schemas");

        List<String> schemaNames = List.of(
                "AuthUserResponse",
                "UserProfileResponse",
                "PerformerProfileResponse",
                "MarketplaceTaskResponse",
                "TaskOfferResponse",
                "DealResponse",
                "DealChatResponse",
                "PaymentTransactionResponse",
                "VerificationRequestResponse",
                "NotificationResponse"
        );

        for (String schemaName : schemaNames) {
            JsonNode schema = schemas.path(schemaName);
            assertThat(schema.isMissingNode())
                    .as("schema %s should exist in OpenAPI components", schemaName)
                    .isFalse();
            assertThat(schema.path("properties").has("id"))
                    .as("schema %s must not expose numeric database id", schemaName)
                    .isFalse();
        }
    }

    private JsonNode openApiSpec() throws Exception {
        String body = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(body);
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
}
