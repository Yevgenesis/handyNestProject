package com.handynest.migration;

import static org.assertj.core.api.Assertions.assertThat;

import com.handynest.HandyNestProjectApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
    classes = HandyNestProjectApplication.class,
    properties = {
      "spring.datasource.url=jdbc:tc:postgresql:16.2:///production-bootstrap-db",
      "spring.liquibase.contexts=production"
    })
@ActiveProfiles("test")
class ProductionLiquibaseContextTest {

  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  void productionBootstrapContainsReferencesButNoDevOrTestUsers() {
    Integer users = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM handy_user", Integer.class);
    Integer publicMvpRoots =
        jdbcTemplate.queryForObject(
            """
                SELECT COUNT(*) FROM category
                 WHERE parent_id IS NULL AND active AND public_visible AND launch_phase = 'MVP'
                """,
            Integer.class);
    Integer settings =
        jdbcTemplate.queryForObject("SELECT COUNT(*) FROM platform_setting", Integer.class);
    Integer uzRegions =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM region r JOIN country c ON c.id = r.country_id WHERE c.code = 'UZ' AND r.is_supported",
            Integer.class);
    Integer uzCities =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM city ct JOIN region r ON r.id = ct.region_id JOIN country c ON c.id = r.country_id WHERE c.code = 'UZ' AND ct.is_supported",
            Integer.class);

    assertThat(users).isZero();
    assertThat(publicMvpRoots).isEqualTo(5);
    assertThat(settings).isGreaterThanOrEqualTo(20);
    assertThat(uzRegions).isEqualTo(14);
    assertThat(uzCities).isEqualTo(120);
  }
}
