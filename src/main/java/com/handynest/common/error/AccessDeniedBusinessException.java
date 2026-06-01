package com.handynest.common.error;

import com.handynest.common.api.ApiErrorCode;
import org.springframework.http.HttpStatus;

public class AccessDeniedBusinessException extends BusinessException {

    public AccessDeniedBusinessException(String message) {
        super(ApiErrorCode.ACCESS_DENIED, HttpStatus.FORBIDDEN, message);
    }
}
