package com.handynest.auth.verification;

import com.handynest.identity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "otp_code")
public class OtpCode {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "phone_number", nullable = false, length = 32)
  private String phoneNumber;

  @Enumerated(EnumType.STRING)
  @Column(name = "purpose", nullable = false, length = 40)
  private OtpPurpose purpose;

  @Column(name = "code_hash", nullable = false, length = 64)
  private String codeHash;

  @Column(name = "attempts", nullable = false)
  private int attempts;

  @Column(name = "max_attempts", nullable = false)
  private int maxAttempts;

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

  protected OtpCode() {}

  public OtpCode(
      User user,
      String phoneNumber,
      OtpPurpose purpose,
      String codeHash,
      int maxAttempts,
      Instant expiresAt,
      Instant resendAvailableAt,
      Instant createdAt,
      String createdByIp) {
    this.user = user;
    this.phoneNumber = phoneNumber;
    this.purpose = purpose;
    this.codeHash = codeHash;
    this.maxAttempts = maxAttempts;
    this.expiresAt = expiresAt;
    this.resendAvailableAt = resendAvailableAt;
    this.createdAt = createdAt;
    this.createdByIp = createdByIp;
  }

  public Long getId() {
    return id;
  }

  public User getUser() {
    return user;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public OtpPurpose getPurpose() {
    return purpose;
  }

  public String getCodeHash() {
    return codeHash;
  }

  public int getAttempts() {
    return attempts;
  }

  public int getMaxAttempts() {
    return maxAttempts;
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

  public Instant getCreatedAt() {
    return createdAt;
  }

  public boolean isActive(Instant now) {
    return consumedAt == null && expiresAt.isAfter(now) && attempts < maxAttempts;
  }

  public void incrementAttempts() {
    this.attempts++;
  }

  public void consume(Instant consumedAt) {
    this.consumedAt = consumedAt;
  }
}
