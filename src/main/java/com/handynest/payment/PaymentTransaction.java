package com.handynest.payment;

import com.handynest.identity.User;
import com.handynest.common.domain.PublicIdEntity;
import com.handynest.marketplace.Deal;
import com.handynest.marketplace.MarketplaceTask;
import com.handynest.marketplace.PaymentMode;
import com.handynest.marketplace.PaymentStatus;
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
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import lombok.Getter;

@Entity
@Table(name = "payment_transaction")
public class PaymentTransaction extends PublicIdEntity {

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(nullable = false)
    private long version;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "deal_id", nullable = false)
    private Deal deal;

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
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Getter
    @Column(name = "platform_fee_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal platformFeeAmount;

    @Getter
    @Column(name = "performer_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal performerAmount;

    @Getter
    @Column(nullable = false, length = 3)
    private String currency;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PaymentStatus status = PaymentStatus.CREATED;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode", nullable = false, length = 32)
    private PaymentMode paymentMode;

    @Getter
    @Column(name = "psp_provider", length = 80)
    private String pspProvider;

    @Getter
    @Column(name = "psp_payment_id", length = 120)
    private String pspPaymentId;

    @Getter
    @Column(name = "psp_reference", length = 160)
    private String pspReference;

    @Getter
    @Column(name = "idempotency_key", nullable = false, length = 120)
    private String idempotencyKey;

    @Getter
    @Column(name = "failure_reason", length = 1000)
    private String failureReason;

    @Getter
    @Column(name = "authorized_at")
    private Instant authorizedAt;

    @Getter
    @Column(name = "held_at")
    private Instant heldAt;

    @Getter
    @Column(name = "released_at")
    private Instant releasedAt;

    @Getter
    @Column(name = "refunded_at")
    private Instant refundedAt;

    protected PaymentTransaction() {
    }

    public PaymentTransaction(
            Deal deal,
            BigDecimal amount,
            String currency,
            PaymentMode paymentMode,
            String idempotencyKey
    ) {
        this.deal = deal;
        this.task = deal.getTask();
        this.customer = deal.getCustomer();
        this.performer = deal.getPerformer();
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
        this.platformFeeAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        this.performerAmount = this.amount;
        this.currency = currency;
        this.paymentMode = paymentMode;
        this.idempotencyKey = idempotencyKey;
    }

    public void attachPspReference(String pspProvider, String pspPaymentId, String pspReference) {
        this.pspProvider = pspProvider;
        this.pspPaymentId = pspPaymentId;
        this.pspReference = pspReference;
    }

    public void transitionTo(PaymentStatus targetStatus, Instant now, String failureReason) {
        boolean allowed = switch (targetStatus) {
            case PENDING -> status == PaymentStatus.CREATED;
            case AUTHORIZED -> status == PaymentStatus.PENDING;
            case HELD -> status == PaymentStatus.AUTHORIZED;
            case CAPTURED -> status == PaymentStatus.HELD;
            case RELEASED -> status == PaymentStatus.HELD || status == PaymentStatus.CAPTURED;
            case REFUNDED, PARTIALLY_REFUNDED -> status == PaymentStatus.HELD
                    || status == PaymentStatus.CAPTURED
                    || status == PaymentStatus.RELEASED;
            case FAILED -> status == PaymentStatus.PENDING;
            case CANCELED -> status == PaymentStatus.AUTHORIZED;
            case CREATED, NOT_REQUIRED -> false;
        };
        if (!allowed) {
            throw new PaymentStatusTransitionException(status, targetStatus);
        }

        this.status = targetStatus;
        this.failureReason = targetStatus == PaymentStatus.FAILED ? failureReason : null;
        if (targetStatus == PaymentStatus.AUTHORIZED) {
            this.authorizedAt = now;
        } else if (targetStatus == PaymentStatus.HELD) {
            this.heldAt = now;
        } else if (targetStatus == PaymentStatus.RELEASED) {
            this.releasedAt = now;
        } else if (targetStatus == PaymentStatus.REFUNDED || targetStatus == PaymentStatus.PARTIALLY_REFUNDED) {
            this.refundedAt = now;
        }
    }
}
