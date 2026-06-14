package com.handynest.common.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RequestCorrelationFilter extends OncePerRequestFilter {

  public static final String REQUEST_ID_HEADER = "X-Request-Id";
  public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

  private static final int MAX_HEADER_LENGTH = 120;
  private static final String REQUEST_ID_MDC_KEY = "requestId";
  private static final String CORRELATION_ID_MDC_KEY = "correlationId";

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {
    String requestId = normalizeId(request.getHeader(REQUEST_ID_HEADER));
    String correlationId = normalizeId(request.getHeader(CORRELATION_ID_HEADER));
    if (requestId == null) {
      requestId = UUID.randomUUID().toString();
    }
    if (correlationId == null) {
      correlationId = requestId;
    }

    response.setHeader(REQUEST_ID_HEADER, requestId);
    response.setHeader(CORRELATION_ID_HEADER, correlationId);
    MDC.put(REQUEST_ID_MDC_KEY, requestId);
    MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
    try {
      filterChain.doFilter(request, response);
    } finally {
      MDC.remove(REQUEST_ID_MDC_KEY);
      MDC.remove(CORRELATION_ID_MDC_KEY);
    }
  }

  private String normalizeId(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    String normalized = value.trim();
    if (normalized.length() > MAX_HEADER_LENGTH) {
      return null;
    }
    return normalized;
  }
}
