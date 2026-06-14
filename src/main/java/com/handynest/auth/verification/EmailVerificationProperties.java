package com.handynest.auth.verification;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.security.email-verification")
public class EmailVerificationProperties {

  private boolean enabled = false;
  private Duration ttl = Duration.ofHours(24);
  private Duration resendCooldown = Duration.ofHours(1);

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

  public Duration getResendCooldown() {
    return resendCooldown;
  }

  public void setResendCooldown(Duration resendCooldown) {
    this.resendCooldown = resendCooldown;
  }
}
