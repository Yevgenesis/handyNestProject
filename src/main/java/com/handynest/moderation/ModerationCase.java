package com.handynest.moderation;

import com.handynest.common.domain.PublicIdEntity;
import com.handynest.common.error.AccessDeniedBusinessException;
import com.handynest.common.error.InvalidStatusTransitionException;
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
import jakarta.persistence.Version;
import java.time.Instant;
import lombok.Getter;

@Entity
@Table(name = "moderation_case")
public class ModerationCase extends PublicIdEntity {

  @Getter
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Version
  @Column(nullable = false)
  private long version;

  @Getter
  @Enumerated(EnumType.STRING)
  @Column(name = "target_type", nullable = false, length = 32)
  private ModerationTargetType targetType;

  @Getter
  @Column(name = "target_id", nullable = false, length = 64)
  private String targetId;

  @Getter
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "opened_by_user_id")
  private User openedByUser;

  @Getter
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "assigned_admin_id")
  private User assignedAdmin;

  @Getter
  @Column(nullable = false, length = 500)
  private String reason;

  @Getter
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private ModerationCaseStatus status = ModerationCaseStatus.OPEN;

  @Getter
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private ModerationPriority priority = ModerationPriority.NORMAL;

  @Getter
  @Enumerated(EnumType.STRING)
  @Column(length = 32)
  private ModerationDecision decision;

  @Getter
  @Column(name = "decision_comment", length = 1000)
  private String decisionComment;

  @Getter
  @Column(name = "resolved_at")
  private Instant resolvedAt;

  protected ModerationCase() {}

  public ModerationCase(
      ModerationTargetType targetType,
      String targetId,
      User openedByUser,
      String reason,
      ModerationPriority priority) {
    this.targetType = targetType;
    this.targetId = targetId;
    this.openedByUser = openedByUser;
    this.reason = reason;
    this.priority = priority == null ? ModerationPriority.NORMAL : priority;
  }

  public void assignTo(User admin) {
    this.assignedAdmin = admin;
  }

  public void startReview(User admin) {
    if (this.status != ModerationCaseStatus.OPEN) {
      throw new InvalidStatusTransitionException(
          "ModerationCase", this.status.name(), ModerationCaseStatus.IN_REVIEW.name());
    }
    this.assignedAdmin = admin;
    this.status = ModerationCaseStatus.IN_REVIEW;
  }

  public void close(
      ModerationCaseStatus status,
      ModerationDecision decision,
      String comment,
      Instant resolvedAt) {
    if (this.resolvedAt != null) {
      throw new InvalidStatusTransitionException("ModerationCase", this.status.name(), "FINAL");
    }
    this.status = status;
    this.decision = decision;
    this.decisionComment = comment;
    this.resolvedAt = resolvedAt;
  }

  public void cancel(User admin, String comment, Instant resolvedAt) {
    assertAssignedTo(admin);
    close(ModerationCaseStatus.CANCELED, ModerationDecision.CANCELED, comment, resolvedAt);
  }

  public void assertAssignedTo(User admin) {
    if (assignedAdmin == null || !assignedAdmin.getId().equals(admin.getId())) {
      throw new AccessDeniedBusinessException(
          "Only the assigned administrator can finish this moderation case");
    }
  }
}
