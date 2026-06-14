package com.handynest.common.error;

import com.handynest.common.api.ApiErrorCode;
import org.springframework.http.HttpStatus;

public class InvalidStatusTransitionException extends BusinessException {

  public InvalidStatusTransitionException(String aggregate, String fromStatus, String toStatus) {
    super(
        ApiErrorCode.INVALID_STATUS_TRANSITION,
        HttpStatus.CONFLICT,
        aggregate + " cannot transition from " + fromStatus + " to " + toStatus);
  }
}
