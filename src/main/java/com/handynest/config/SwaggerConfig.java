package com.handynest.config;

import com.handynest.common.api.ApiConstants;
import com.handynest.common.api.ApiErrorResponse;
import com.handynest.common.api.ApiFieldError;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info =
        @Info(
            title = "HandyNest API",
            version = "v1",
            description = "Uzbekistan-first transactional marketplace backend API."))
public class SwaggerConfig {

  private static final String BEARER_AUTH = "bearerAuth";
  private static final String API_ERROR_RESPONSE_REF = "#/components/schemas/ApiErrorResponse";

  @Bean
  public OpenAPI customOpenAPI() {
    Components components =
        new Components()
            .addSecuritySchemes(
                BEARER_AUTH,
                new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT"))
            .addResponses("BadRequest", errorResponse("Bad request"))
            .addResponses("Unauthorized", errorResponse("Authentication required"))
            .addResponses("Forbidden", errorResponse("Access denied"))
            .addResponses("NotFound", errorResponse("Resource not found"))
            .addResponses("RateLimited", errorResponse("Too many requests"))
            .addResponses("InternalError", errorResponse("Internal error"));
    ModelConverters.getInstance().read(ApiFieldError.class).forEach(components::addSchemas);
    ModelConverters.getInstance().read(ApiErrorResponse.class).forEach(components::addSchemas);

    return new OpenAPI()
        .info(
            new io.swagger.v3.oas.models.info.Info()
                .title("HandyNest API")
                .version("v1")
                .description("Public /api/v1 contract for the HandyNest backend.")
                .contact(new Contact().name("HandyNest")))
        .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
        .components(components);
  }

  @Bean
  public OpenApiCustomizer handyNestContractCustomizer() {
    return openApi ->
        openApi
            .getPaths()
            .forEach(
                (path, pathItem) ->
                    pathItem
                        .readOperationsMap()
                        .forEach(
                            (method, operation) -> {
                              operation.setTags(List.of(tagForPath(path)));
                              applySecurity(path, method, operation);
                              applyStandardErrorResponses(path, method, operation);
                              applyIdempotencyHeader(path, method, operation);
                            }));
  }

  private ApiResponse errorResponse(String description) {
    return new ApiResponse()
        .description(description)
        .content(
            new Content()
                .addMediaType(
                    org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                    new MediaType().schema(new Schema<>().$ref(API_ERROR_RESPONSE_REF))));
  }

  private void applySecurity(String path, PathItem.HttpMethod method, Operation operation) {
    if (isPublicEndpoint(path, method)) {
      operation.setSecurity(Collections.emptyList());
      return;
    }
    operation.setSecurity(List.of(new SecurityRequirement().addList(BEARER_AUTH)));
  }

  private void applyStandardErrorResponses(
      String path, PathItem.HttpMethod method, Operation operation) {
    operation
        .getResponses()
        .addApiResponse("400", new ApiResponse().$ref("#/components/responses/BadRequest"));
    operation
        .getResponses()
        .addApiResponse("429", new ApiResponse().$ref("#/components/responses/RateLimited"));
    operation
        .getResponses()
        .addApiResponse("500", new ApiResponse().$ref("#/components/responses/InternalError"));

    if (!isPublicEndpoint(path, method)) {
      operation
          .getResponses()
          .addApiResponse("401", new ApiResponse().$ref("#/components/responses/Unauthorized"));
      operation
          .getResponses()
          .addApiResponse("403", new ApiResponse().$ref("#/components/responses/Forbidden"));
    }
    if (path.contains("{")) {
      operation
          .getResponses()
          .addApiResponse("404", new ApiResponse().$ref("#/components/responses/NotFound"));
    }
  }

  private void applyIdempotencyHeader(
      String path, PathItem.HttpMethod method, Operation operation) {
    if (method != PathItem.HttpMethod.POST || !requiresIdempotency(path)) {
      return;
    }
    Parameter idempotencyKey = null;
    if (operation.getParameters() != null) {
      idempotencyKey =
          operation.getParameters().stream()
              .filter(
                  parameter ->
                      ApiConstants.IDEMPOTENCY_KEY_HEADER.equals(parameter.getName())
                          && "header".equals(parameter.getIn()))
              .findFirst()
              .orElse(null);
    }
    if (idempotencyKey == null) {
      idempotencyKey = new Parameter().in("header").name(ApiConstants.IDEMPOTENCY_KEY_HEADER);
      operation.addParametersItem(idempotencyKey);
    }
    idempotencyKey
        .required(true)
        .description(
            "Required idempotency key for retry-safe critical POST operations. "
                + "Use a unique string per user action; max length is 120 characters.")
        .schema(new StringSchema().maxLength(120));
  }

  private boolean isPublicEndpoint(String path, PathItem.HttpMethod method) {
    if (method == PathItem.HttpMethod.POST
        && (path.equals(ApiConstants.API_V1 + "/auth/register")
            || path.equals(ApiConstants.API_V1 + "/auth/login")
            || path.equals(ApiConstants.API_V1 + "/auth/refresh")
            || path.equals(ApiConstants.API_V1 + "/auth/verify-email")
            || path.equals(ApiConstants.API_V1 + "/payments/webhooks/{provider}"))) {
      return true;
    }
    if (method != PathItem.HttpMethod.GET) {
      return false;
    }
    return path.startsWith(ApiConstants.API_V1 + "/categories")
        || path.startsWith(ApiConstants.API_V1 + "/geo")
        || path.equals(ApiConstants.API_V1 + "/market/config")
        || path.equals(ApiConstants.API_V1 + "/legal/consent-requirements")
        || path.equals(ApiConstants.API_V1 + "/performers")
        || path.matches("^" + ApiConstants.API_V1 + "/performers/\\{[^/]+}$")
        || path.equals(ApiConstants.API_V1 + "/tasks")
        || path.matches("^" + ApiConstants.API_V1 + "/tasks/\\{[^/]+}$")
        || path.matches("^" + ApiConstants.API_V1 + "/tasks/\\{[^/]+}/attachments$")
        || path.matches(
            "^" + ApiConstants.API_V1 + "/tasks/\\{[^/]+}/attachments/\\{[^/]+}/download-url$")
        || path.matches("^" + ApiConstants.API_V1 + "/tasks/\\{[^/]+}/feedbacks$")
        || path.matches("^" + ApiConstants.API_V1 + "/users/\\{[^/]+}/feedbacks$")
        || path.matches("^" + ApiConstants.API_V1 + "/performers/\\{[^/]+}/feedbacks$");
  }

  private boolean requiresIdempotency(String path) {
    return path.equals(ApiConstants.API_V1 + "/tasks")
        || path.matches("^" + ApiConstants.API_V1 + "/tasks/\\{[^/]+}/repeat$")
        || path.matches("^" + ApiConstants.API_V1 + "/tasks/\\{[^/]+}/offers$")
        || path.matches(
            "^" + ApiConstants.API_V1 + "/tasks/\\{[^/]+}/offers/\\{[^/]+}/(accept|cancel)$")
        || path.matches(
            "^"
                + ApiConstants.API_V1
                + "/chats/\\{[^/]+}/(submit-work|accept-work|request-revision|open-dispute)$")
        || path.matches("^" + ApiConstants.API_V1 + "/tasks/\\{[^/]+}/feedbacks$")
        || path.matches("^" + ApiConstants.API_V1 + "/deals/\\{[^/]+}/(payments|milestones)$")
        || path.matches("^" + ApiConstants.API_V1 + "/deals/\\{[^/]+}/(contact-reveals|cancel)$")
        || path.matches(
            "^"
                + ApiConstants.API_V1
                + "/payments/\\{[^/]+}/(authorize|hold|release|refund|fail|cancel)$")
        || path.equals(ApiConstants.API_V1 + "/payments/webhooks/{provider}")
        || path.matches(
            "^"
                + ApiConstants.API_V1
                + "/milestones/\\{[^/]+}/(start|submit|accept|reject|dispute|cancel)$")
        || path.equals(ApiConstants.API_V1 + "/complaints")
        || path.matches("^" + ApiConstants.API_V1 + "/disputes/\\{[^/]+}/attachments$")
        || path.matches("^" + ApiConstants.API_V1 + "/admin/disputes/\\{[^/]+}/resolve$")
        || path.equals(ApiConstants.API_V1 + "/admin/categories");
  }

  private String tagForPath(String path) {
    String normalized = path.toLowerCase(Locale.ROOT);
    if (normalized.contains("/admin/tasks")) {
      return "Admin Marketplace";
    }
    if (normalized.contains("/admin/categories")) {
      return "Admin Categories";
    }
    if (normalized.contains("/admin/disputes")) {
      return "Disputes";
    }
    if (normalized.contains("/admin/verification-requests")
        || normalized.contains("/admin/performer-categories")
        || (normalized.contains("/admin/performers") && normalized.contains("/categories"))) {
      return "Admin Verification";
    }
    if (normalized.contains("/auth")) {
      return "Auth";
    }
    if (normalized.contains("/users") && !normalized.contains("/feedbacks")) {
      return "Users";
    }
    if (normalized.contains("/favorite-performers")) {
      return "Marketplace Favorites";
    }
    if ((normalized.contains("/tasks/") && normalized.contains("/offers"))
        || normalized.contains("/my/offers")) {
      return "Task Offers";
    }
    if (normalized.contains("/feedbacks")) {
      return "Feedback";
    }
    if (normalized.contains("/verification")) {
      return "Verification";
    }
    if (normalized.contains("/performers")) {
      return "Performers";
    }
    if (normalized.contains("/categories")) {
      return "Categories";
    }
    if (normalized.contains("/geo")) {
      return "Geo";
    }
    if (normalized.contains("/market/config")) {
      return "Market";
    }
    if (normalized.contains("/tasks")) {
      return "Marketplace Tasks";
    }
    if (normalized.contains("/deals") || normalized.contains("/milestones")) {
      return "Deals";
    }
    if (normalized.contains("/chats")) {
      return "Chats";
    }
    if (normalized.contains("/disputes")) {
      return "Disputes";
    }
    if (normalized.contains("/attachments") || normalized.contains("/documents")) {
      return "Marketplace Files";
    }
    if (normalized.contains("/moderation") || normalized.contains("/complaints")) {
      return "Moderation";
    }
    if (normalized.contains("/risk-events")) {
      return "Risk";
    }
    if (normalized.contains("/payments")) {
      return "Payments";
    }
    if (normalized.contains("/notifications")) {
      return "Notifications";
    }
    return "HandyNest";
  }
}
