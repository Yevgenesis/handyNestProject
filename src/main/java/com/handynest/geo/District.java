package com.handynest.geo;

import com.handynest.common.domain.PublicIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "district")
public class District extends PublicIdEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "city_id", nullable = false)
  private City city;

  @Column(name = "name_ru", nullable = false, length = 120)
  private String nameRu;

  @Column(name = "name_kz", nullable = false, length = 120)
  private String nameKz;

  @Column(name = "name_en", nullable = false, length = 120)
  private String nameEn;

  @Column(name = "name_uz", nullable = false, length = 120)
  private String nameUz;

  @Column(nullable = false, unique = true, length = 120)
  private String slug;

  @Column(precision = 10, scale = 7)
  private BigDecimal latitude;

  @Column(precision = 10, scale = 7)
  private BigDecimal longitude;

  @Column(name = "is_supported", nullable = false)
  private boolean supported;

  protected District() {}

  public Long getId() {
    return id;
  }

  public City getCity() {
    return city;
  }

  public String getNameRu() {
    return nameRu;
  }

  public String getNameKz() {
    return nameKz;
  }

  public String getNameEn() {
    return nameEn;
  }

  public String getNameUz() {
    return nameUz;
  }

  public String getSlug() {
    return slug;
  }

  public BigDecimal getLatitude() {
    return latitude;
  }

  public BigDecimal getLongitude() {
    return longitude;
  }

  public boolean isSupported() {
    return supported;
  }
}
