package com.handynest.platform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.handynest.HandyNestProjectApplication;
import com.handynest.testsupport.TestDatabaseConfig;
import java.math.BigDecimal;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@ContextConfiguration(classes = {TestDatabaseConfig.class, HandyNestProjectApplication.class})
class PlatformSettingServiceTest {

  @Autowired private PlatformSettingService service;

  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  void readsEverySupportedSettingType() {
    assertThat(service.booleanValue(PlatformSettingKey.PHONE_VERIFICATION_ENABLED)).isTrue();
    assertThat(service.integerValue(PlatformSettingKey.FREE_OFFER_LIMIT_MONTHLY)).isEqualTo(10);
    assertThat(service.decimalValue(PlatformSettingKey.MILESTONE_THRESHOLD_KZT))
        .isEqualByComparingTo(new BigDecimal("500000.00"));
    assertThat(service.stringValue(PlatformSettingKey.TERMS_VERSION)).isEqualTo("1.0");
    assertThat(service.durationValue(PlatformSettingKey.OUTBOX_RETRY_BASE_DELAY))
        .isEqualTo(Duration.ofMinutes(1));
  }

  @Test
  void rejectsMalformedTypedValue() {
    jdbcTemplate.update(
        "UPDATE platform_setting SET setting_value = 'not-an-integer' WHERE setting_key = ?",
        PlatformSettingKey.FREE_OFFER_LIMIT_MONTHLY.key());
    try {
      assertThatThrownBy(() -> service.integerValue(PlatformSettingKey.FREE_OFFER_LIMIT_MONTHLY))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining(PlatformSettingKey.FREE_OFFER_LIMIT_MONTHLY.key());
    } finally {
      jdbcTemplate.update(
          "UPDATE platform_setting SET setting_value = '10' WHERE setting_key = ?",
          PlatformSettingKey.FREE_OFFER_LIMIT_MONTHLY.key());
    }
  }
}
