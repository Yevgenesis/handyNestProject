package com.handynest.payment;

import com.handynest.common.error.InvalidStatusTransitionException;
import com.handynest.marketplace.PaymentStatus;

public class PaymentStatusTransitionException extends InvalidStatusTransitionException {

  public PaymentStatusTransitionException(PaymentStatus currentStatus, PaymentStatus targetStatus) {
    super("PaymentTransaction", currentStatus.name(), targetStatus.name());
  }
}
