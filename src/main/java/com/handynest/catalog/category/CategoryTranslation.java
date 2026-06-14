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
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "category_translation",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uc_category_translation_category_locale",
            columnNames = {"category_id", "locale"}))
public class CategoryTranslation extends BaseAuditEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "category_id", nullable = false)
  private Category category;

  @Column(nullable = false, length = 8)
  private String locale;

  @Column(nullable = false, length = 255)
  private String name;

  @Column(length = 2000)
  private String description;

  protected CategoryTranslation() {}

  public CategoryTranslation(Category category, String locale, String name, String description) {
    this.category = category;
    this.locale = locale;
    this.name = name;
    this.description = description;
  }

  public Long getId() {
    return id;
  }

  public Category getCategory() {
    return category;
  }

  public String getLocale() {
    return locale;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }
}
