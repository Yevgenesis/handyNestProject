package com.handynest.identity;

import com.handynest.geo.City;
import com.handynest.geo.Country;
import com.handynest.geo.District;
import com.handynest.marketplace.legacy.Feedback;
import com.handynest.marketplace.legacy.Message;
import com.handynest.marketplace.legacy.Task;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "handy_user")
@EntityListeners(AuditingEntityListener.class)
@Schema(description = "Entity representing a user")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Schema(description = "Unique identifier of the user", example = "1")
  private Long id;

  @Column(name = "public_id", nullable = false, unique = true, updatable = false, length = 26)
  @Schema(description = "Public user id", example = "01KAE5YFES4D72W93NS1Q3D4VT")
  private String publicId;

  @Column(name = "first_name", nullable = false, length = 50)
  @Schema(description = "First name of the user", example = "John")
  private String firstName;

  @Column(name = "last_name", nullable = false, length = 50)
  @Schema(description = "Last name of the user", example = "Doe")
  private String lastName;

  @Column(name = "email", nullable = false, length = 50, unique = true)
  @Schema(description = "Email of the user", example = "john.doe@example.com")
  private String email;

  @Column(name = "phone", length = 32)
  @Schema(description = "Phone of the user", example = "+998901234567")
  private String phone;

  @Column(name = "is_email_verified", nullable = false)
  @Schema(description = "Indicates if the email is verified", example = "true")
  private boolean isEmailVerified;

  @Column(name = "is_phone_verified", nullable = false)
  @Schema(description = "Indicates if the phone is verified", example = "false")
  private boolean isPhoneVerified;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 32)
  @Schema(description = "User status", example = "ACTIVE")
  private UserStatus status = UserStatus.ACTIVE;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(name = "account_type", nullable = false, length = 32)
  @Schema(description = "User account type", example = "PERSONAL")
  private AccountType accountType = AccountType.PERSONAL;

  @Column(name = "password", nullable = false, length = 100)
  @Schema(description = "Password of the user", example = "password123")
  private String password;

  @Builder.Default
  @Column(name = "task_count", nullable = false)
  @Schema(description = "Number of tasks created by the user", example = "5")
  private Long taskCount = 0L;

  @Column(name = "is_deleted", nullable = false)
  @Schema(description = "Indicates if the user is deleted", example = "false")
  private boolean isDeleted;

  @Builder.Default
  @OneToMany(
      mappedBy = "user",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @Schema(description = "Tasks created by the user")
  private Set<Task> tasks = new HashSet<>();

  @Column(name = "logo")
  @Schema(description = "Logo of the user", example = "logo.png")
  private String logo;

  @Column(name = "avatar_attachment_id")
  @Schema(description = "Avatar attachment id")
  private Long avatarAttachmentId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "country_id")
  private Country country;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "city_id")
  private City city;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "district_id")
  private District district;

  @Column(name = "preferred_service_radius_km")
  private Integer preferredServiceRadiusKm;

  @Builder.Default
  @Column(name = "customer_risk_score", nullable = false, precision = 5, scale = 2)
  private BigDecimal customerRiskScore = BigDecimal.ZERO;

  @Column(name = "deleted_at")
  private Instant deletedAt;

  @CreatedDate
  @Column(name = "created_on", nullable = false, updatable = false)
  @Schema(description = "Date when the user was created", example = "2024-01-01")
  private Timestamp created_on;

  @LastModifiedDate
  @Column(name = "updated_on", nullable = false)
  @Schema(description = "Date when the user was last updated", example = "2024-01-01")
  private Timestamp updated_on;

  @Builder.Default
  @ElementCollection(targetClass = RoleName.class, fetch = FetchType.EAGER)
  @CollectionTable(
      name = "user_roles",
      joinColumns = @JoinColumn(name = "user_id"),
      uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "role_name"}))
  @Column(name = "role_name")
  @Enumerated(EnumType.STRING)
  @Schema(description = "Roles assigned to the user")
  private Set<RoleName> roles = new HashSet<>();

  @Builder.Default
  @OneToMany(
      mappedBy = "sender",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @Schema(description = "Feedbacks sent by the user")
  private Set<Feedback> sentFeedbacks = new HashSet<>();

  @Builder.Default
  @OneToMany(
      mappedBy = "sender",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @Schema(description = "Messages sent by the user")
  private Set<Message> sendMessages = new HashSet<>();

  @Column(name = "user_rating")
  @Builder.Default
  @Schema(description = "Positive feedback percentage of the user", example = "100.0")
  private Double positiveFeedbackPercent = 100.0;

  public void increaseTaskCounter() {
    this.taskCount++;
  }

  public void addCustomerRiskScore(BigDecimal points) {
    if (points == null || points.signum() <= 0) {
      return;
    }
    BigDecimal current = customerRiskScore == null ? BigDecimal.ZERO : customerRiskScore;
    this.customerRiskScore = current.add(points).min(new BigDecimal("999.99"));
  }

  public void subtractCustomerRiskScore(BigDecimal points) {
    if (points == null || points.signum() <= 0) {
      return;
    }
    BigDecimal current = customerRiskScore == null ? BigDecimal.ZERO : customerRiskScore;
    this.customerRiskScore = current.subtract(points).max(BigDecimal.ZERO);
  }
}
