package com.handynest.common.error;

import com.handynest.common.api.ApiErrorCode;
import org.springframework.http.HttpStatus;

public class BadRequestBusinessException extends BusinessException {

  public BadRequestBusinessException(String message) {
    super(ApiErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, message);
  }
}
