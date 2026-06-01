package com.handynest.common.api;

public record ApiFieldError(
        String field,
        String message
) {
}
