package com.handynest.moderation;

import com.handynest.identity.User;
import com.handynest.common.domain.PublicIdEntity;
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

@Entity
@Table(name = "complaint")
public class Complaint extends PublicIdEntity {

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporter_user_id", nullable = false)
    private User reporter;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_user_id")
    private User targetUser;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 32)
    private ModerationTargetType targetType;

    @Getter
    @Column(name = "target_id", nullable = false, length = 64)
    private String targetId;

    @Getter
    @Column(nullable = false, length = 500)
    private String reason;

    @Getter
    @Column(nullable = false, length = 4000)
    private String description;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ComplaintStatus status = ComplaintStatus.OPEN;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moderation_case_id")
    private ModerationCase moderationCase;

    @Getter
    @Column(name = "resolved_at")
    private Instant resolvedAt;

    protected Complaint() {
    }

    public Complaint(
            User reporter,
            User targetUser,
            ModerationTargetType targetType,
            String targetId,
            String reason,
            String description
    ) {
        this.reporter = reporter;
        this.targetUser = targetUser;
        this.targetType = targetType;
        this.targetId = targetId;
        this.reason = reason;
        this.description = description;
    }

    public void attachModerationCase(ModerationCase moderationCase) {
        this.moderationCase = moderationCase;
    }

    public void markInReview() {
        this.status = ComplaintStatus.IN_REVIEW;
    }

    public void markResolved(Instant resolvedAt) {
        this.status = ComplaintStatus.RESOLVED;
        this.resolvedAt = resolvedAt;
    }
}
