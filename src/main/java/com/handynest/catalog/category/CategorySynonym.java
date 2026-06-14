package com.handynest.catalog.category;

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

@Entity
@Table(name = "category_synonym")
public class CategorySynonym extends BaseAuditEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "category_id", nullable = false)
  private Category category;

  @Column(nullable = false, length = 8)
  private String locale;

  @Column(nullable = false, length = 160)
  private String value;

  @Column(name = "normalized_value", nullable = false, length = 160)
  private String normalizedValue;

  protected CategorySynonym() {}

  public CategorySynonym(Category category, String locale, String value, String normalizedValue) {
    this.category = category;
    this.locale = locale;
    this.value = value;
    this.normalizedValue = normalizedValue;
  }

  public Category getCategory() {
    return category;
  }

  public String getLocale() {
    return locale;
  }

  public String getValue() {
    return value;
  }

  public String getNormalizedValue() {
    return normalizedValue;
  }
}
