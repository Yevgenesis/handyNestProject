package com.handynest.marketplace;

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import lombok.Getter;

@Entity
@Table(name = "dispute_case")
public class DisputeCase extends PublicIdEntity {

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(nullable = false)
    private long version;

    @Getter
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "deal_id", nullable = false)
    private Deal deal;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private MarketplaceTask task;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "opened_by_user_id", nullable = false)
    private User openedByUser;

    @Getter
    @Column(nullable = false, length = 160)
    private String reason;

    @Getter
    @Column(length = 4000)
    private String description;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private DisputeCaseStatus status = DisputeCaseStatus.OPEN;

    @Getter
    @Column(name = "admin_decision", length = 4000)
    private String adminDecision;

    @Getter
    @Column(name = "resolved_at")
    private Instant resolvedAt;

    protected DisputeCase() {
    }

    public DisputeCase(Deal deal, User openedByUser, String reason, String description) {
        this.deal = deal;
        this.task = deal.getTask();
        this.openedByUser = openedByUser;
        this.reason = reason;
        this.description = description;
    }

    public void markUnderReview() {
        this.status = DisputeCaseStatus.UNDER_REVIEW;
    }

    public void resolve(DisputeCaseStatus status, String adminDecision, Instant resolvedAt) {
        this.status = status;
        this.adminDecision = adminDecision;
        this.resolvedAt = resolvedAt;
    }
}
