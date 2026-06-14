package com.handynest.common.error;

import com.handynest.common.api.ApiErrorCode;
import org.springframework.http.HttpStatus;

public class ConsentRequiredException extends BusinessException {

  public ConsentRequiredException(String message) {
    super(ApiErrorCode.CONSENT_REQUIRED, HttpStatus.CONFLICT, message);
  }
}
