package com.handynest.common.api;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpStatus;

public record ApiErrorResponse(
    Instant timestamp,
    int status,
    String error,
    String code,
    String message,
    String path,
    List<ApiFieldError> details) {

  public ApiErrorResponse {
    timestamp = Objects.requireNonNullElseGet(timestamp, Instant::now);
    details = details == null ? List.of() : List.copyOf(details);
  }

  public static ApiErrorResponse of(
      HttpStatus status, ApiErrorCode code, String message, String path) {
    return of(status, code, message, path, List.of());
  }

  public static ApiErrorResponse of(
      HttpStatus status,
      ApiErrorCode code,
      String message,
      String path,
      List<ApiFieldError> details) {
    return new ApiErrorResponse(
        Instant.now(),
        status.value(),
        status.getReasonPhrase(),
        code.name(),
        message,
        path,
        details);
  }
}
