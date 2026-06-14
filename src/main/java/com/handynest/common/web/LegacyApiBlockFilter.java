package com.handynest.common.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.handynest.common.api.ApiErrorCode;
import com.handynest.common.api.ApiErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class LegacyApiBlockFilter extends OncePerRequestFilter {

  private static final String MESSAGE = "Legacy API is disabled; use /api/v1";
  private static final List<String> LEGACY_PATH_PREFIXES =
      List.of(
          "/attachments",
          "/categories",
          "/chats",
          "/feedbacks",
          "/messages",
          "/performers",
          "/tasks",
          "/users");

  private final ObjectMapper objectMapper;

  public LegacyApiBlockFilter(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {
    String path = pathWithoutContext(request);
    if (!isLegacyApiPath(path)) {
      filterChain.doFilter(request, response);
      return;
    }

    response.setStatus(HttpStatus.GONE.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    objectMapper.writeValue(
        response.getWriter(),
        ApiErrorResponse.of(HttpStatus.GONE, ApiErrorCode.LEGACY_API_DISABLED, MESSAGE, path));
  }

  private boolean isLegacyApiPath(String path) {
    return LEGACY_PATH_PREFIXES.stream()
        .anyMatch(prefix -> path.equals(prefix) || path.startsWith(prefix + "/"));
  }

  private String pathWithoutContext(HttpServletRequest request) {
    String requestUri = request.getRequestURI();
    String contextPath = request.getContextPath();
    if (contextPath == null || contextPath.isBlank() || !requestUri.startsWith(contextPath)) {
      return requestUri;
    }
    return requestUri.substring(contextPath.length());
  }
}
