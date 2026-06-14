package com.handynest.auth.verification;

public interface EmailProvider {

  void sendVerificationToken(String email, String token);
}
