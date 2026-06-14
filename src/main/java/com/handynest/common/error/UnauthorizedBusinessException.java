package com.handynest.common.error;

import com.handynest.common.api.ApiErrorCode;
import org.springframework.http.HttpStatus;

public class UnauthorizedBusinessException extends BusinessException {

  public UnauthorizedBusinessException(String message) {
    super(ApiErrorCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED, message);
  }
}
