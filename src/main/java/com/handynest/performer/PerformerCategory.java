package com.handynest.performer;

import codezilla.handynestproject.model.entity.Category;
import com.handynest.common.domain.BaseAuditEntity;
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
import java.math.BigDecimal;

@Entity
@Table(
        name = "performer_category",
        uniqueConstraints = @UniqueConstraint(
                name = "uc_performer_category_profile_category",
                columnNames = {"performer_profile_id", "category_id"}
        )
)
public class PerformerCategory extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "performer_profile_id", nullable = false)
    private PerformerProfile performerProfile;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Column(name = "price_from", precision = 12, scale = 2)
    private BigDecimal priceFrom;

    @Column(name = "price_to", precision = 12, scale = 2)
    private BigDecimal priceTo;

    @Column(nullable = false, length = 3)
    private String currency = "KZT";

    @Column(name = "is_primary", nullable = false)
    private boolean primaryCategory;

    protected PerformerCategory() {
    }

    public PerformerCategory(Category category) {
        this.category = category;
    }

    public Long getId() {
        return id;
    }

    public PerformerProfile getPerformerProfile() {
        return performerProfile;
    }

    void setPerformerProfile(PerformerProfile performerProfile) {
        this.performerProfile = performerProfile;
    }

    public Category getCategory() {
        return category;
    }

    public Integer getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(Integer experienceYears) {
        this.experienceYears = experienceYears;
    }

    public BigDecimal getPriceFrom() {
        return priceFrom;
    }

    public void setPriceFrom(BigDecimal priceFrom) {
        this.priceFrom = priceFrom;
    }

    public BigDecimal getPriceTo() {
        return priceTo;
    }

    public void setPriceTo(BigDecimal priceTo) {
        this.priceTo = priceTo;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public boolean isPrimaryCategory() {
        return primaryCategory;
    }

    public void setPrimaryCategory(boolean primaryCategory) {
        this.primaryCategory = primaryCategory;
    }
}
