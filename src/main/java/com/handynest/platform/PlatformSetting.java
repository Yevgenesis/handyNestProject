package com.handynest.platform;

import com.handynest.common.domain.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "platform_setting")
public class PlatformSetting extends BaseAuditEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "setting_key", nullable = false, unique = true, length = 120)
  private String key;

  @Column(name = "setting_value", nullable = false, length = 1000)
  private String value;

  @Enumerated(EnumType.STRING)
  @Column(name = "value_type", nullable = false, length = 20)
  private PlatformSettingValueType valueType;

  @Column(nullable = false, length = 500)
  private String description;

  protected PlatformSetting() {}
}
