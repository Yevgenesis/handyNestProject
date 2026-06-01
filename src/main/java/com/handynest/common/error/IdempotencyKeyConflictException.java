package com.handynest.common.error;

import com.handynest.common.api.ApiErrorCode;
import org.springframework.http.HttpStatus;

public class IdempotencyKeyConflictException extends BusinessException {

    public IdempotencyKeyConflictException() {
        super(
                ApiErrorCode.IDEMPOTENCY_KEY_CONFLICT,
                HttpStatus.CONFLICT,
                "Idempotency key was already used with a different request"
        );
    }
}
