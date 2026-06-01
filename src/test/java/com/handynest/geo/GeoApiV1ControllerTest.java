package com.handynest.geo;

import codezilla.handynestproject.HandyNestProjectApplication;
import codezilla.handynestproject.util.TestDatabaseConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(classes = {TestDatabaseConfig.class, HandyNestProjectApplication.class})
class GeoApiV1ControllerTest {

    private static final String ALMATY_REGION_PUBLIC_ID = "06KZRG00000000000000000001";
    private static final String ALMATY_CITY_PUBLIC_ID = "06KZCT00000000000000000001";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void countriesReturnsKazakhstanWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/geo/countries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code").value("KZ"))
                .andExpect(jsonPath("$[0].name").value("Казахстан"))
                .andExpect(jsonPath("$[0].phoneCode").value("+7"))
                .andExpect(jsonPath("$[0].currencyCode").value("KZT"))
                .andExpect(jsonPath("$[0].supported").value(true))
                .andExpect(jsonPath("$[0].nameRu").doesNotExist());
    }

    @Test
    void countriesUsesAcceptLanguage() throws Exception {
        mockMvc.perform(get("/api/v1/geo/countries")
                        .header(HttpHeaders.ACCEPT_LANGUAGE, "en-US,en;q=0.9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Kazakhstan"));
    }

    @Test
    void regionsReturnsKazakhstanRegionsByCountryCode() throws Exception {
        mockMvc.perform(get("/api/v1/geo/countries/KZ/regions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(15)))
                .andExpect(jsonPath("$[*].publicId", hasItem(ALMATY_REGION_PUBLIC_ID)))
                .andExpect(jsonPath("$[*].name", hasItem("Алматы")))
                .andExpect(jsonPath("$[0].id").doesNotExist())
                .andExpect(jsonPath("$[0].nameRu").doesNotExist());
    }

    @Test
    void citiesReturnsMvpCitiesByRegionPublicId() throws Exception {
        mockMvc.perform(get("/api/v1/geo/regions/{regionId}/cities", ALMATY_REGION_PUBLIC_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].publicId").value(ALMATY_CITY_PUBLIC_ID))
                .andExpect(jsonPath("$[0].name").value("Алматы"))
                .andExpect(jsonPath("$[0].slug").value("almaty"))
                .andExpect(jsonPath("$[0].sortOrder").value(10))
                .andExpect(jsonPath("$[0].id").doesNotExist())
                .andExpect(jsonPath("$[0].nameRu").doesNotExist());
    }

    @Test
    void districtsReturnsEmptyListWhenCityHasNoDistrictsYet() throws Exception {
        mockMvc.perform(get("/api/v1/geo/cities/{cityId}/districts", ALMATY_CITY_PUBLIC_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
