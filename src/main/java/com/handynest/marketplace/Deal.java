package com.handynest.marketplace;

import com.handynest.identity.User;
import com.handynest.common.domain.PublicIdEntity;
import com.handynest.common.error.InvalidStatusTransitionException;
import com.handynest.performer.PerformerProfile;
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
@Table(name = "deal")
public class Deal extends PublicIdEntity {

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(nullable = false)
    private long version;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private MarketplaceTask task;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "performer_id", nullable = false)
    private PerformerProfile performer;

    @Getter
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "accepted_offer_id", nullable = false)
    private TaskOffer acceptedOffer;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private DealStatus status = DealStatus.ACTIVE;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode", nullable = false, length = 32)
    private PaymentMode paymentMode = PaymentMode.OFF_PLATFORM;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 32)
    private PaymentStatus paymentStatus = PaymentStatus.NOT_REQUIRED;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "contact_visibility_status", nullable = false, length = 32)
    private ContactVisibilityStatus contactVisibilityStatus = ContactVisibilityStatus.HIDDEN;

    @Getter
    @Column(name = "milestone_enabled", nullable = false)
    private boolean milestoneEnabled;

    @Getter
    @Column(name = "revision_count", nullable = false)
    private int revisionCount;

    @Getter
    @Column(name = "completed_at")
    private Instant completedAt;

    @Getter
    @Column(name = "canceled_at")
    private Instant canceledAt;

    protected Deal() {
    }

    public Deal(MarketplaceTask task, TaskOffer acceptedOffer) {
        this.task = task;
        this.customer = task.getCustomer();
        this.performer = acceptedOffer.getPerformer();
        this.acceptedOffer = acceptedOffer;
    }

    public void enableMilestones() {
        this.milestoneEnabled = true;
    }

    public void submitWork() {
        requireDealStatus(DealStatus.ACTIVE, DealStatus.REVISION_REQUESTED);
        this.status = DealStatus.WORK_SUBMITTED;
    }

    public void complete(Instant completedAt) {
        requireDealStatus(DealStatus.WORK_SUBMITTED, DealStatus.DISPUTED);
        this.status = DealStatus.COMPLETED;
        this.completedAt = completedAt;
    }

    public void requestRevision() {
        requireDealStatus(DealStatus.WORK_SUBMITTED);
        this.status = DealStatus.REVISION_REQUESTED;
        this.revisionCount++;
    }

    public void resumeAfterRevision() {
        requireDealStatus(DealStatus.REVISION_REQUESTED);
        this.status = DealStatus.ACTIVE;
    }

    public void openDispute() {
        requireDealStatus(DealStatus.ACTIVE, DealStatus.WORK_SUBMITTED, DealStatus.REVISION_REQUESTED);
        this.status = DealStatus.DISPUTED;
    }

    public void resolveDisputeCompleted(Instant completedAt) {
        requireDealStatus(DealStatus.DISPUTED);
        this.status = DealStatus.COMPLETED;
        this.completedAt = completedAt;
    }

    public void resolveDisputeCanceled(Instant canceledAt) {
        requireDealStatus(DealStatus.DISPUTED);
        this.status = DealStatus.CANCELED;
        this.canceledAt = canceledAt;
    }

    public void cancelByAdminOrDispute(Instant canceledAt) {
        requireDealStatus(DealStatus.ACTIVE, DealStatus.DISPUTED);
        this.status = DealStatus.CANCELED;
        this.canceledAt = canceledAt;
    }

    public void updatePayment(PaymentMode paymentMode, PaymentStatus paymentStatus) {
        this.paymentMode = paymentMode;
        this.paymentStatus = paymentStatus;
    }

    private void requireDealStatus(DealStatus... allowedStatuses) {
        for (DealStatus allowedStatus : allowedStatuses) {
            if (this.status == allowedStatus) {
                return;
            }
        }
        throw new InvalidStatusTransitionException(
                "Deal",
                this.status.name(),
                allowedStatuses[0].name()
        );
    }
}
