package com.handynest.common.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.handynest.common.api.ApiErrorCode;
import com.handynest.common.api.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApiSecurityErrorWriter {

  private final ObjectMapper objectMapper;

  public void write(
      HttpServletRequest request,
      HttpServletResponse response,
      HttpStatus status,
      ApiErrorCode code,
      String message)
      throws IOException {
    if (response.isCommitted()) {
      return;
    }

    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    objectMapper.writeValue(
        response.getOutputStream(),
        ApiErrorResponse.of(status, code, message, request.getRequestURI()));
  }
}
