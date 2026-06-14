package com.handynest.risk;

import com.handynest.common.domain.PublicIdEntity;
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
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;

@Entity
@Table(
    name = "customer_risk_adjustment",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_customer_risk_adjustment_source",
            columnNames = {"source_type", "source_public_id"}))
public class CustomerRiskAdjustment extends PublicIdEntity {

  @Getter
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Getter
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Getter
  @Enumerated(EnumType.STRING)
  @Column(name = "source_type", nullable = false, length = 32)
  private CustomerRiskSourceType sourceType;

  @Getter
  @Column(name = "source_public_id", nullable = false, length = 26)
  private String sourcePublicId;

  @Getter
  @Column(nullable = false, precision = 5, scale = 2)
  private BigDecimal points;

  @Getter
  @Column(nullable = false, length = 500)
  private String reason;

  @Getter
  @Column(name = "reversed_at")
  private Instant reversedAt;

  protected CustomerRiskAdjustment() {}

  public CustomerRiskAdjustment(
      User user,
      CustomerRiskSourceType sourceType,
      String sourcePublicId,
      BigDecimal points,
      String reason) {
    this.user = user;
    this.sourceType = sourceType;
    this.sourcePublicId = sourcePublicId;
    this.points = points;
    this.reason = reason;
  }

  public boolean isActive() {
    return reversedAt == null;
  }

  public void reverse(Instant reversedAt) {
    if (this.reversedAt == null) {
      this.reversedAt = reversedAt;
    }
  }
}
