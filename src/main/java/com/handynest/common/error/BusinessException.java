package com.handynest.common.error;

import com.handynest.common.api.ApiErrorCode;
import org.springframework.http.HttpStatus;

public abstract class BusinessException extends RuntimeException {

  private final ApiErrorCode code;
  private final HttpStatus status;

  protected BusinessException(ApiErrorCode code, HttpStatus status, String message) {
    super(message);
    this.code = code;
    this.status = status;
  }

  public ApiErrorCode getCode() {
    return code;
  }

  public HttpStatus getStatus() {
    return status;
  }
}
