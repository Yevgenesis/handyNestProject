package com.handynest.common.error;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.handynest.common.api.ApiErrorResponse;
import com.handynest.common.web.ApiExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

class ApiExceptionHandlerTest {

  @Test
  void handleBusinessExceptionReturnsUnifiedErrorContract() {
    ApiExceptionHandler handler = new ApiExceptionHandler();
    MockHttpServletRequest request =
        new MockHttpServletRequest("GET", "/api/v1/tasks/01JZ7Y6F4C6XKQ3M6YB4F9M1AA");

    ResponseEntity<ApiErrorResponse> response =
        handler.handleBusinessException(
            new ResourceNotFoundException("Task", "01JZ7Y6F4C6XKQ3M6YB4F9M1AA"), request);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(404, response.getBody().status());
    assertEquals("NOT_FOUND", response.getBody().code());
    assertEquals("/api/v1/tasks/01JZ7Y6F4C6XKQ3M6YB4F9M1AA", response.getBody().path());
  }
}
