package com.handynest.auth.verification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NoopEmailProvider implements EmailProvider {

  private static final Logger log = LoggerFactory.getLogger(NoopEmailProvider.class);

  @Override
  public void sendVerificationToken(String email, String token) {
    log.info("Email verification requested for {}", maskEmail(email));
  }

  private String maskEmail(String email) {
    if (email == null || !email.contains("@")) {
      return "****";
    }
    String[] parts = email.split("@", 2);
    String local = parts[0].isEmpty() ? "*" : parts[0].substring(0, 1) + "***";
    return local + "@" + parts[1];
  }
}
