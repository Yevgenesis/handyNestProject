package com.handynest.common.error;

import com.handynest.common.api.ApiErrorCode;
import org.springframework.http.HttpStatus;

public class ConflictBusinessException extends BusinessException {

    public ConflictBusinessException(String message) {
        super(ApiErrorCode.CONFLICT, HttpStatus.CONFLICT, message);
    }
}
