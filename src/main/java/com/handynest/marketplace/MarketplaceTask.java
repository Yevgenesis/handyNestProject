package com.handynest.marketplace;

import com.handynest.catalog.category.Category;
import com.handynest.identity.User;
import com.handynest.catalog.category.CategoryServiceMode;
import com.handynest.common.domain.PublicIdEntity;
import com.handynest.common.error.InvalidStatusTransitionException;
import com.handynest.geo.City;
import com.handynest.geo.Country;
import com.handynest.geo.District;
import com.handynest.geo.Region;
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
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "marketplace_task")
public class MarketplaceTask extends PublicIdEntity {

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(nullable = false)
    private long version;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Setter
    @Getter
    @Column(nullable = false, length = 160)
    private String title;

    @Setter
    @Getter
    @Column(nullable = false, length = 4000)
    private String description;

    @Setter
    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "service_mode", nullable = false, length = 20)
    private CategoryServiceMode serviceMode;

    @Setter
    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "price_type", nullable = false, length = 20)
    private PriceType priceType;

    @Setter
    @Getter
    @Column(name = "budget_min", precision = 19, scale = 2)
    private BigDecimal budgetMin;

    @Setter
    @Getter
    @Column(name = "budget_max", precision = 19, scale = 2)
    private BigDecimal budgetMax;

    @Setter
    @Getter
    @Column(name = "fixed_price", precision = 19, scale = 2)
    private BigDecimal fixedPrice;

    @Setter
    @Getter
    @Column(nullable = false, length = 3)
    private String currency = "KZT";

    @Getter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private Region region;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id")
    private District district;

    @Setter
    @Getter
    @Column(name = "address_text", length = 500)
    private String addressText;

    @Setter
    @Getter
    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Setter
    @Getter
    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TaskStatus status = TaskStatus.DRAFT;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "publication_status", nullable = false, length = 32)
    private PublicationStatus publicationStatus = PublicationStatus.UNPUBLISHED;

    @Setter
    @Getter
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_offer_id")
    private TaskOffer selectedOffer;

    @Setter
    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_performer_id")
    private PerformerProfile selectedPerformer;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repeat_of_task_id")
    private MarketplaceTask repeatOfTask;

    @Setter
    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preferred_performer_id")
    private PerformerProfile preferredPerformer;

    @Getter
    @Column(name = "revision_count", nullable = false)
    private int revisionCount;

    @Setter
    @Getter
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Getter
    @Column(name = "expired_at")
    private Instant expiredAt;

    @Getter
    @Column(name = "accepted_at")
    private Instant acceptedAt;

    @Getter
    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Getter
    @Column(name = "completed_at")
    private Instant completedAt;

    @Getter
    @Column(name = "canceled_at")
    private Instant canceledAt;

    protected MarketplaceTask() {
    }

    public MarketplaceTask(User customer, Category category, City city) {
        this.customer = customer;
        this.category = category;
        setCity(city);
    }

    public void setCity(City city) {
        this.city = city;
        this.region = city == null ? null : city.getRegion();
        this.country = city == null ? null : city.getCountry();
    }

    public void setDistrict(District district) {
        this.district = district;
        if (district != null) {
            setCity(district.getCity());
        }
    }

    public void markAsRepeatOf(MarketplaceTask repeatOfTask) {
        this.repeatOfTask = repeatOfTask;
    }

    public void submitForModeration() {
        requireTaskStatus(TaskStatus.DRAFT, TaskStatus.MODERATION);
        this.status = TaskStatus.MODERATION;
        this.publicationStatus = PublicationStatus.UNPUBLISHED;
    }

    public void publish() {
        requireTaskStatus(TaskStatus.DRAFT, TaskStatus.MODERATION, TaskStatus.OPEN);
        this.status = TaskStatus.OPEN;
        this.publicationStatus = PublicationStatus.PUBLISHED;
    }

    public void rejectByModeration() {
        requireTaskStatus(TaskStatus.DRAFT, TaskStatus.MODERATION, TaskStatus.OPEN);
        this.publicationStatus = PublicationStatus.REJECTED_BY_MODERATION;
        if (this.status == TaskStatus.OPEN) {
            this.status = TaskStatus.MODERATION;
        }
    }

    public void expire(Instant expiredAt) {
        requireTaskStatus(TaskStatus.OPEN);
        this.status = TaskStatus.EXPIRED;
        this.publicationStatus = PublicationStatus.UNPUBLISHED;
        this.expiredAt = expiredAt;
    }

    public void cancelOpen(Instant canceledAt) {
        requireTaskStatus(TaskStatus.OPEN);
        this.status = TaskStatus.CANCELED;
        this.publicationStatus = PublicationStatus.UNPUBLISHED;
        this.canceledAt = canceledAt;
    }

    public void acceptOffer(TaskOffer offer, Instant acceptedAt) {
        requireTaskStatus(TaskStatus.OPEN);
        this.status = TaskStatus.IN_PROGRESS;
        this.selectedOffer = offer;
        this.selectedPerformer = offer.getPerformer();
        this.acceptedAt = acceptedAt;
    }

    public void submitWork(Instant submittedAt) {
        requireTaskStatus(TaskStatus.IN_PROGRESS, TaskStatus.REVISION_REQUESTED);
        this.status = TaskStatus.WORK_SUBMITTED;
        this.submittedAt = submittedAt;
    }

    public void completeWork(Instant completedAt) {
        requireTaskStatus(TaskStatus.WORK_SUBMITTED, TaskStatus.DISPUTED);
        this.status = TaskStatus.COMPLETED;
        this.completedAt = completedAt;
    }

    public void requestRevision() {
        requireTaskStatus(TaskStatus.WORK_SUBMITTED);
        this.status = TaskStatus.REVISION_REQUESTED;
        this.revisionCount++;
    }

    public void resumeAfterRevision() {
        requireTaskStatus(TaskStatus.REVISION_REQUESTED);
        this.status = TaskStatus.IN_PROGRESS;
    }

    public void openDispute() {
        requireTaskStatus(TaskStatus.WORK_SUBMITTED, TaskStatus.IN_PROGRESS, TaskStatus.REVISION_REQUESTED);
        this.status = TaskStatus.DISPUTED;
    }

    public void resolveDisputeCompleted(Instant completedAt) {
        requireTaskStatus(TaskStatus.DISPUTED);
        this.status = TaskStatus.COMPLETED;
        this.completedAt = completedAt;
    }

    public void resolveDisputeCanceled(Instant canceledAt) {
        requireTaskStatus(TaskStatus.DISPUTED);
        this.status = TaskStatus.CANCELED;
        this.publicationStatus = PublicationStatus.UNPUBLISHED;
        this.canceledAt = canceledAt;
    }

    private void requireTaskStatus(TaskStatus... allowedStatuses) {
        for (TaskStatus allowedStatus : allowedStatuses) {
            if (this.status == allowedStatus) {
                return;
            }
        }
        throw new InvalidStatusTransitionException(
                "Task",
                this.status.name(),
                allowedStatuses[0].name()
        );
    }
}
