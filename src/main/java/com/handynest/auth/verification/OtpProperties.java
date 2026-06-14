package com.handynest.auth.verification;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.security.otp")
public class OtpProperties {

  private boolean enabled = true;
  private Duration ttl = Duration.ofMinutes(5);
  private int maxAttempts = 5;
  private Duration resendCooldown = Duration.ofSeconds(60);

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public Duration getTtl() {
    return ttl;
  }

  public void setTtl(Duration ttl) {
    this.ttl = ttl;
  }

  public int getMaxAttempts() {
    return maxAttempts;
  }

  public void setMaxAttempts(int maxAttempts) {
    this.maxAttempts = maxAttempts;
  }

  public Duration getResendCooldown() {
    return resendCooldown;
  }

  public void setResendCooldown(Duration resendCooldown) {
    this.resendCooldown = resendCooldown;
  }
}
