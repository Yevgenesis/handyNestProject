package com.handynest.common.error;

import com.handynest.common.api.ApiErrorCode;
import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String resourceName, String publicId) {
        super(ApiErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, resourceName + " not found: " + publicId);
    }
}
