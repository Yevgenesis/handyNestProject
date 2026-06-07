package com.handynest.identity;

import com.handynest.common.domain.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "customer_profile")
public class CustomerProfile extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "rating_average", nullable = false, precision = 3, scale = 2)
    private BigDecimal ratingAverage = BigDecimal.ZERO;

    @Column(name = "rating_count", nullable = false)
    private long ratingCount;

    @Column(name = "completed_orders_count", nullable = false)
    private long completedOrdersCount;

    @Column(name = "canceled_orders_count", nullable = false)
    private long canceledOrdersCount;

    @Column(name = "dispute_count", nullable = false)
    private long disputeCount;

    @Column(name = "no_show_count", nullable = false)
    private long noShowCount;

    protected CustomerProfile() {
    }

    public CustomerProfile(User user) {
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public BigDecimal getRatingAverage() {
        return ratingAverage;
    }

    public long getRatingCount() {
        return ratingCount;
    }

    public long getCompletedOrdersCount() {
        return completedOrdersCount;
    }

    public long getCanceledOrdersCount() {
        return canceledOrdersCount;
    }

    public long getDisputeCount() {
        return disputeCount;
    }

    public long getNoShowCount() {
        return noShowCount;
    }

    public void applyRating(int grade, long previousRatingCount) {
        this.ratingAverage = recalculateRatingAverage(grade, previousRatingCount);
        this.ratingCount = previousRatingCount + 1;
    }

    private BigDecimal recalculateRatingAverage(int grade, long previousRatingCount) {
        BigDecimal previousTotal = ratingAverage.multiply(BigDecimal.valueOf(previousRatingCount));
        return previousTotal
                .add(BigDecimal.valueOf(grade))
                .divide(BigDecimal.valueOf(previousRatingCount + 1), 2, RoundingMode.HALF_UP);
    }
}
