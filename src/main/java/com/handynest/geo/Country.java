package com.handynest.geo;

import com.handynest.common.domain.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "country")
public class Country extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 2)
    private String code;

    @Column(name = "name_ru", nullable = false, length = 120)
    private String nameRu;

    @Column(name = "name_kz", nullable = false, length = 120)
    private String nameKz;

    @Column(name = "name_en", nullable = false, length = 120)
    private String nameEn;

    @Column(name = "phone_code", nullable = false, length = 8)
    private String phoneCode;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Column(name = "is_supported", nullable = false)
    private boolean supported;

    protected Country() {
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
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

    public String getPhoneCode() {
        return phoneCode;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public boolean isSupported() {
        return supported;
    }
}
