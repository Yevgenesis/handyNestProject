package com.handynest.common.error;

import com.handynest.common.api.ApiErrorCode;
import org.springframework.http.HttpStatus;

public class DuplicateResourceException extends BusinessException {

    public DuplicateResourceException(String message) {
        super(ApiErrorCode.DUPLICATE_RESOURCE, HttpStatus.CONFLICT, message);
    }
}
