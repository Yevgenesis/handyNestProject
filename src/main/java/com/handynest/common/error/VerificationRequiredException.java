package com.handynest.common.error;

import com.handynest.common.api.ApiErrorCode;
import org.springframework.http.HttpStatus;

public class VerificationRequiredException extends BusinessException {

  public VerificationRequiredException(String message) {
    super(ApiErrorCode.VERIFICATION_REQUIRED, HttpStatus.FORBIDDEN, message);
  }
}
