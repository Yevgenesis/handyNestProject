package com.handynest.platform;

import java.math.BigDecimal;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlatformSettingService {

  private final PlatformSettingRepository repository;

  @Transactional(readOnly = true)
  public boolean booleanValue(PlatformSettingKey key) {
    PlatformSetting setting = setting(key, PlatformSettingValueType.BOOLEAN);
    if (!"true".equalsIgnoreCase(setting.getValue())
        && !"false".equalsIgnoreCase(setting.getValue())) {
      throw invalid(setting);
    }
    return Boolean.parseBoolean(setting.getValue());
  }

  @Transactional(readOnly = true)
  public int integerValue(PlatformSettingKey key) {
    PlatformSetting setting = setting(key, PlatformSettingValueType.INTEGER);
    try {
      return Integer.parseInt(setting.getValue());
    } catch (NumberFormatException exception) {
      throw invalid(setting);
    }
  }

  @Transactional(readOnly = true)
  public BigDecimal decimalValue(PlatformSettingKey key) {
    PlatformSetting setting = setting(key, PlatformSettingValueType.DECIMAL);
    try {
      return new BigDecimal(setting.getValue());
    } catch (NumberFormatException exception) {
      throw invalid(setting);
    }
  }

  @Transactional(readOnly = true)
  public Duration durationValue(PlatformSettingKey key) {
    PlatformSetting setting = setting(key, PlatformSettingValueType.DURATION);
    try {
      return Duration.parse(setting.getValue());
    } catch (RuntimeException exception) {
      throw invalid(setting);
    }
  }

  @Transactional(readOnly = true)
  public String stringValue(PlatformSettingKey key) {
    return setting(key, PlatformSettingValueType.STRING).getValue();
  }

  private PlatformSetting setting(PlatformSettingKey key, PlatformSettingValueType expectedType) {
    PlatformSetting setting =
        repository
            .findByKey(key.key())
            .orElseThrow(() -> new IllegalStateException("Missing platform setting: " + key.key()));
    if (setting.getValueType() != expectedType) {
      throw invalid(setting);
    }
    return setting;
  }

  private IllegalStateException invalid(PlatformSetting setting) {
    return new IllegalStateException("Invalid platform setting: " + setting.getKey());
  }
}
