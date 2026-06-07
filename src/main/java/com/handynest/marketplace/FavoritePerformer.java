package com.handynest.marketplace;

import com.handynest.identity.User;
import com.handynest.common.domain.BaseAuditEntity;
import com.handynest.performer.PerformerProfile;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;

@Entity
@Table(
        name = "favorite_performer",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_favorite_performer_customer_performer",
                columnNames = {"customer_id", "performer_profile_id"}
        )
)
public class FavoritePerformer extends BaseAuditEntity {

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "performer_profile_id", nullable = false)
    private PerformerProfile performerProfile;

    @Getter
    @Column(length = 500)
    private String note;

    protected FavoritePerformer() {
    }

    public FavoritePerformer(User customer, PerformerProfile performerProfile, String note) {
        this.customer = customer;
        this.performerProfile = performerProfile;
        this.note = note;
    }
}
