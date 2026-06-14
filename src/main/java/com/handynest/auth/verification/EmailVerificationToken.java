package com.handynest.auth.verification;

import com.handynest.identity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "email_verification_token")
public class EmailVerificationToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "email", nullable = false, length = 255)
  private String email;

  @Column(name = "token_hash", nullable = false, unique = true, length = 64)
  private String tokenHash;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Column(name = "consumed_at")
  private Instant consumedAt;

  @Column(name = "resend_available_at", nullable = false)
  private Instant resendAvailableAt;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "created_by_ip", length = 64)
  private String createdByIp;

  protected EmailVerificationToken() {}

  public EmailVerificationToken(
      User user,
      String email,
      String tokenHash,
      Instant expiresAt,
      Instant resendAvailableAt,
      Instant createdAt,
      String createdByIp) {
    this.user = user;
    this.email = email;
    this.tokenHash = tokenHash;
    this.expiresAt = expiresAt;
    this.resendAvailableAt = resendAvailableAt;
    this.createdAt = createdAt;
    this.createdByIp = createdByIp;
  }

  public User getUser() {
    return user;
  }

  public String getEmail() {
    return email;
  }

  public String getTokenHash() {
    return tokenHash;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(Instant expiresAt) {
    this.expiresAt = expiresAt;
  }

  public Instant getConsumedAt() {
    return consumedAt;
  }

  public Instant getResendAvailableAt() {
    return resendAvailableAt;
  }

  public boolean isActive(Instant now) {
    return consumedAt == null && expiresAt.isAfter(now);
  }

  public void consume(Instant consumedAt) {
    this.consumedAt = consumedAt;
  }
}
