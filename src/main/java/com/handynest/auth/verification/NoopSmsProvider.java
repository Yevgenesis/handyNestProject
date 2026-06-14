package com.handynest.auth.verification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NoopSmsProvider implements SmsProvider {

  private static final Logger log = LoggerFactory.getLogger(NoopSmsProvider.class);

  @Override
  public void sendOtp(String phoneNumber, String otpCode) {
    log.info("Phone OTP requested for {}", maskPhone(phoneNumber));
  }

  private String maskPhone(String phoneNumber) {
    if (phoneNumber == null || phoneNumber.length() <= 4) {
      return "****";
    }
    return "****" + phoneNumber.substring(phoneNumber.length() - 4);
  }
}
