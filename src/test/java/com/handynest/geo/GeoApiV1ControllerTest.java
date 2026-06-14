package com.handynest.geo;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.handynest.HandyNestProjectApplication;
import com.handynest.testsupport.TestDatabaseConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(classes = {TestDatabaseConfig.class, HandyNestProjectApplication.class})
class GeoApiV1ControllerTest {

  private static final String TASHKENT_REGION_PUBLIC_ID = "06UZRG00000000000000000001";
  private static final String TASHKENT_CITY_PUBLIC_ID = "06UZCT00000000000000000001";
  private static final String ANDIJAN_REGION_PUBLIC_ID = "06UZRG00000000000000000003";

  @Autowired private MockMvc mockMvc;

  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  void marketConfigReturnsUzbekistanLaunchDefaults() throws Exception {
    mockMvc
        .perform(get("/api/v1/market/config"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.defaultCountryCode").value("UZ"))
        .andExpect(jsonPath("$.defaultCityId").value(TASHKENT_CITY_PUBLIC_ID))
        .andExpect(jsonPath("$.defaultCityName").value("Ташкент"))
        .andExpect(jsonPath("$.defaultCurrency").value("UZS"))
        .andExpect(jsonPath("$.defaultLocale").value("ru"))
        .andExpect(jsonPath("$.supportedLocales", hasSize(2)))
        .andExpect(jsonPath("$.supportedLocales", hasItem("ru")))
        .andExpect(jsonPath("$.supportedLocales", hasItem("uz")));
  }

  @Test
  void kazakhstanGeoIsPreservedButNotPubliclySupported() throws Exception {
    Integer countryCount =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM country WHERE code = 'KZ'", Integer.class);
    Boolean countrySupported =
        jdbcTemplate.queryForObject(
            "SELECT is_supported FROM country WHERE code = 'KZ'", Boolean.class);
    Integer cityCount =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM city WHERE country_id = (SELECT id FROM country WHERE code = 'KZ')",
            Integer.class);

    org.junit.jupiter.api.Assertions.assertEquals(1, countryCount);
    org.junit.jupiter.api.Assertions.assertEquals(Boolean.FALSE, countrySupported);
    org.junit.jupiter.api.Assertions.assertNotNull(cityCount);
    org.junit.jupiter.api.Assertions.assertTrue(cityCount > 0);

    mockMvc
        .perform(get("/api/v1/geo/countries/KZ/regions"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  void countriesReturnsUzbekistanWithoutAuthentication() throws Exception {
    mockMvc
        .perform(get("/api/v1/geo/countries"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].code").value("UZ"))
        .andExpect(jsonPath("$[0].name").value("Узбекистан"))
        .andExpect(jsonPath("$[0].phoneCode").value("+998"))
        .andExpect(jsonPath("$[0].currencyCode").value("UZS"))
        .andExpect(jsonPath("$[0].supported").value(true))
        .andExpect(jsonPath("$[0].nameRu").doesNotExist());
  }

  @Test
  void countriesUsesAcceptLanguage() throws Exception {
    mockMvc
        .perform(get("/api/v1/geo/countries").header(HttpHeaders.ACCEPT_LANGUAGE, "uz-UZ,uz;q=0.9"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].name").value("O‘zbekiston"));
  }

  @Test
  void regionsReturnsAllUzbekistanRegionsByCountryCode() throws Exception {
    mockMvc
        .perform(get("/api/v1/geo/countries/UZ/regions"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(14)))
        .andExpect(jsonPath("$[*].publicId", hasItem(TASHKENT_REGION_PUBLIC_ID)))
        .andExpect(jsonPath("$[*].publicId", hasItem(ANDIJAN_REGION_PUBLIC_ID)))
        .andExpect(jsonPath("$[*].name", hasItem("город Ташкент")))
        .andExpect(jsonPath("$[0].id").doesNotExist())
        .andExpect(jsonPath("$[0].nameRu").doesNotExist());
  }

  @Test
  void citiesReturnsMvpCitiesByRegionPublicId() throws Exception {
    mockMvc
        .perform(get("/api/v1/geo/regions/{regionId}/cities", TASHKENT_REGION_PUBLIC_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].publicId").value(TASHKENT_CITY_PUBLIC_ID))
        .andExpect(jsonPath("$[0].name").value("Ташкент"))
        .andExpect(jsonPath("$[0].slug").value("tashkent"))
        .andExpect(jsonPath("$[0].sortOrder").value(10))
        .andExpect(jsonPath("$[0].id").doesNotExist())
        .andExpect(jsonPath("$[0].nameRu").doesNotExist());
  }

  @Test
  void supportedCitiesReturnsCompleteUzbekistanCityClassifier() throws Exception {
    mockMvc
        .perform(get("/api/v1/geo/cities"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(120)))
        .andExpect(jsonPath("$[*].publicId", hasItem(TASHKENT_CITY_PUBLIC_ID)))
        .andExpect(jsonPath("$[*].name", hasItem("Ташкент")))
        .andExpect(jsonPath("$[*].name", hasItem("Самарканд")))
        .andExpect(jsonPath("$[*].name", hasItem("Нукус")))
        .andExpect(jsonPath("$[0].id").doesNotExist());
  }

  @Test
  void regionalCitiesAreLocalizedToUzbekLatin() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/geo/regions/{regionId}/cities", ANDIJAN_REGION_PUBLIC_ID)
                .param("locale", "uz"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(11)))
        .andExpect(jsonPath("$[*].name", hasItem("Andijon")))
        .andExpect(jsonPath("$[*].name", hasItem("Shahrixon")));
  }

  @Test
  void districtsReturnsAllTashkentDistricts() throws Exception {
    mockMvc
        .perform(get("/api/v1/geo/cities/{cityId}/districts", TASHKENT_CITY_PUBLIC_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(12)))
        .andExpect(jsonPath("$[*].name", hasItem("Юнусабадский район")));
  }
}
