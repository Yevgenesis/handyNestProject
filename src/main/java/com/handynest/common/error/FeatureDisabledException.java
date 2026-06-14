package com.handynest.common.error;

import com.handynest.common.api.ApiErrorCode;
import org.springframework.http.HttpStatus;

public class FeatureDisabledException extends BusinessException {

  public FeatureDisabledException(String feature) {
    super(ApiErrorCode.FEATURE_DISABLED, HttpStatus.CONFLICT, feature + " is disabled");
  }
}
