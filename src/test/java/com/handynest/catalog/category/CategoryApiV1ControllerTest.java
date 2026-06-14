package com.handynest.catalog.category;

import static org.hamcrest.Matchers.hasItems;
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
class CategoryApiV1ControllerTest {

  private static final String PLUMBER_PUBLIC_ID = "06CAT000000000000000000011";

  @Autowired private MockMvc mockMvc;

  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  void findAllReturnsPublicCategoryTreeWithoutAuthentication() throws Exception {
    mockMvc
        .perform(get("/api/v1/categories"))
        .andExpect(status().isOk())
        .andExpect(
            jsonPath(
                "$[*].slug",
                hasItems(
                    "plumber", "electrician", "appliance-repair", "cleaning", "freight-transport")))
        .andExpect(jsonPath("$", hasSize(5)))
        .andExpect(jsonPath("$[0].publicId").isString())
        .andExpect(jsonPath("$[0].id").doesNotExist())
        .andExpect(jsonPath("$[0].slug").value("plumber"))
        .andExpect(jsonPath("$[0].riskLevel").value("MEDIUM"))
        .andExpect(jsonPath("$[0].serviceMode").value("ONSITE"))
        .andExpect(jsonPath("$[0].launchPhase").value("MVP"))
        .andExpect(jsonPath("$[0].active").value(true))
        .andExpect(jsonPath("$[0].publicVisible").value(true))
        .andExpect(jsonPath("$[0].sortOrder").value(10))
        .andExpect(jsonPath("$[0].allowsCash").value(true))
        .andExpect(jsonPath("$[0].allowsAttachments").value(true))
        .andExpect(jsonPath("$[0].requiresOnsiteCoordination").value(true))
        .andExpect(jsonPath("$[0].contactRevealStage").value("AFTER_DEAL_CREATED"));
  }

  @Test
  void findByPublicIdReturnsSingleCategoryWithoutInternalId() throws Exception {
    mockMvc
        .perform(get("/api/v1/categories/{categoryId}", PLUMBER_PUBLIC_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.publicId").value(PLUMBER_PUBLIC_ID))
        .andExpect(jsonPath("$.title").value("Сантехник"))
        .andExpect(jsonPath("$.slug").value("plumber"))
        .andExpect(jsonPath("$.id").doesNotExist())
        .andExpect(jsonPath("$.children").isArray());
  }

  @Test
  void findByPublicIdUsesTranslationAndFallsBackToRu() throws Exception {
    jdbcTemplate.update(
        "UPDATE category SET title = 'Legacy title' WHERE public_id = ?", PLUMBER_PUBLIC_ID);

    try {
      mockMvc
          .perform(get("/api/v1/categories/{categoryId}", PLUMBER_PUBLIC_ID))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.title").value("Сантехник"));

      mockMvc
          .perform(
              get("/api/v1/categories/{categoryId}", PLUMBER_PUBLIC_ID).queryParam("locale", "uz"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.title").value("Santexnik"));

      mockMvc
          .perform(
              get("/api/v1/categories/{categoryId}", PLUMBER_PUBLIC_ID).queryParam("locale", "en"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.title").value("Сантехник"));
    } finally {
      jdbcTemplate.update(
          "UPDATE category SET title = 'Сантехник' WHERE public_id = ?", PLUMBER_PUBLIC_ID);
    }
  }

  @Test
  void approvedCatalogHasUniqueSlugsTranslationsAndNoDuplicateTireService() {
    Integer rootCount =
        jdbcTemplate.queryForObject(
            """
                SELECT COUNT(*) FROM category
                 WHERE parent_id IS NULL AND active AND public_visible AND launch_phase = 'MVP'
                   AND slug IN ('plumber', 'electrician', 'appliance-repair', 'cleaning', 'freight-transport')
                """,
            Integer.class);
    Integer duplicateSlugs =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM (SELECT slug FROM category GROUP BY slug HAVING COUNT(*) > 1) duplicate_slug",
            Integer.class);
    Integer categoriesWithoutTranslations =
        jdbcTemplate.queryForObject(
            """
                SELECT COUNT(*)
                  FROM category category
                 WHERE category.active AND category.public_visible AND category.launch_phase = 'MVP'
                   AND (SELECT COUNT(*) FROM category_translation translation
                         WHERE translation.category_id = category.id AND translation.locale IN ('ru', 'uz')) <> 2
                """,
            Integer.class);
    Integer tireServiceCount =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM category WHERE slug = 'tire-service' AND active AND public_visible",
            Integer.class);

    org.junit.jupiter.api.Assertions.assertEquals(5, rootCount);
    org.junit.jupiter.api.Assertions.assertEquals(0, duplicateSlugs);
    org.junit.jupiter.api.Assertions.assertEquals(0, categoriesWithoutTranslations);
    org.junit.jupiter.api.Assertions.assertEquals(0, tireServiceCount);
  }

  @Test
  void findByUnknownPublicIdReturnsUnifiedApiError() throws Exception {
    mockMvc
        .perform(get("/api/v1/categories/{categoryId}", "06F5Z8NMS7YQE4Q2F4R1HN9ZZZ"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.code").value("NOT_FOUND"))
        .andExpect(jsonPath("$.path").value("/api/v1/categories/06F5Z8NMS7YQE4Q2F4R1HN9ZZZ"));
  }
}
