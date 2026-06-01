package com.handynest.common.api;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ApiErrorResponseTest {

    @Test
    void ofBuildsSpecAlignedErrorResponse() {
        List<ApiFieldError> details = new ArrayList<>();
        details.add(new ApiFieldError("title", "must not be blank"));

        ApiErrorResponse response = ApiErrorResponse.of(
                HttpStatus.BAD_REQUEST,
                ApiErrorCode.VALIDATION_ERROR,
                "Validation failed",
                "/api/v1/tasks",
                details
        );

        details.clear();

        assertNotNull(response.timestamp());
        assertEquals(400, response.status());
        assertEquals("Bad Request", response.error());
        assertEquals("VALIDATION_ERROR", response.code());
        assertEquals("Validation failed", response.message());
        assertEquals("/api/v1/tasks", response.path());
        assertEquals(List.of(new ApiFieldError("title", "must not be blank")), response.details());
        assertThrows(UnsupportedOperationException.class,
                () -> response.details().add(new ApiFieldError("price", "must be positive")));
    }
}
