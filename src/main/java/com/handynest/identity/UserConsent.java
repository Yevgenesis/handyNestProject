package com.handynest.identity;

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
import lombok.Getter;

@Getter
@Entity
@Table(name = "user_consent")
public class UserConsent {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(name = "consent_type", nullable = false, length = 48)
  private ConsentType consentType;

  @Column(name = "document_version", nullable = false, length = 32)
  private String documentVersion;

  @Column(name = "accepted_at", nullable = false)
  private Instant acceptedAt;

  @Column(name = "ip_address", nullable = false, length = 64)
  private String ipAddress;

  @Column(name = "user_agent", length = 500)
  private String userAgent;

  protected UserConsent() {}

  public UserConsent(
      User user,
      ConsentType consentType,
      String documentVersion,
      Instant acceptedAt,
      String ipAddress,
      String userAgent) {
    this.user = user;
    this.consentType = consentType;
    this.documentVersion = documentVersion;
    this.acceptedAt = acceptedAt;
    this.ipAddress = ipAddress;
    this.userAgent = userAgent;
  }
}
