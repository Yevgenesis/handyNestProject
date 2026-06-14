package com.handynest.auth.verification;

public interface SmsProvider {

  void sendOtp(String phoneNumber, String otpCode);
}
