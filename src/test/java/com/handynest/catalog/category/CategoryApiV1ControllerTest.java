package com.handynest.catalog.category;

import codezilla.handynestproject.HandyNestProjectApplication;
import codezilla.handynestproject.util.TestDatabaseConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(classes = {TestDatabaseConfig.class, HandyNestProjectApplication.class})
class CategoryApiV1ControllerTest {

    private static final String REPAIR_PUBLIC_ID = "06F5Z8NMS7YQE4Q2F4R1HN9EZG";
    private static final String PHOTOGRAPHY_PUBLIC_ID = "06F5Z8NMTBE8A9HV9JC4AR9AWG";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void findAllReturnsPublicCategoryTreeWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].publicId").value(REPAIR_PUBLIC_ID))
                .andExpect(jsonPath("$[0].id").doesNotExist())
                .andExpect(jsonPath("$[0].slug").value("category-1"))
                .andExpect(jsonPath("$[0].riskLevel").value("LOW"))
                .andExpect(jsonPath("$[0].serviceMode").value("HYBRID"))
                .andExpect(jsonPath("$[0].launchPhase").value("MVP"))
                .andExpect(jsonPath("$[0].active").value(true))
                .andExpect(jsonPath("$[0].publicVisible").value(true))
                .andExpect(jsonPath("$[0].sortOrder").value(10))
                .andExpect(jsonPath("$[0].allowsCash").value(true))
                .andExpect(jsonPath("$[0].allowsAttachments").value(true))
                .andExpect(jsonPath("$[0].children[0].publicId").exists())
                .andExpect(jsonPath("$[9].publicId").value(PHOTOGRAPHY_PUBLIC_ID))
                .andExpect(jsonPath("$[9].children[0].title").value("Свадебная фотография"));
    }

    @Test
    void findByPublicIdReturnsSingleCategoryWithoutInternalId() throws Exception {
        mockMvc.perform(get("/api/v1/categories/{categoryId}", REPAIR_PUBLIC_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publicId").value(REPAIR_PUBLIC_ID))
                .andExpect(jsonPath("$.title").value("Ремонт"))
                .andExpect(jsonPath("$.id").doesNotExist())
                .andExpect(jsonPath("$.children").isArray());
    }

    @Test
    void findByPublicIdUsesTranslationAndFallsBackToRu() throws Exception {
        jdbcTemplate.update("UPDATE category SET title = 'Legacy title' WHERE public_id = ?", REPAIR_PUBLIC_ID);

        try {
            mockMvc.perform(get("/api/v1/categories/{categoryId}", REPAIR_PUBLIC_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Ремонт"));

            mockMvc.perform(get("/api/v1/categories/{categoryId}", REPAIR_PUBLIC_ID)
                            .queryParam("locale", "kk"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Ремонт"));

            mockMvc.perform(get("/api/v1/categories/{categoryId}", REPAIR_PUBLIC_ID)
                            .queryParam("locale", "en"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Ремонт"));
        } finally {
            jdbcTemplate.update("UPDATE category SET title = 'Ремонт' WHERE public_id = ?", REPAIR_PUBLIC_ID);
        }
    }

    @Test
    void findByUnknownPublicIdReturnsUnifiedApiError() throws Exception {
        mockMvc.perform(get("/api/v1/categories/{categoryId}", "06F5Z8NMS7YQE4Q2F4R1HN9ZZZ"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.path").value("/api/v1/categories/06F5Z8NMS7YQE4Q2F4R1HN9ZZZ"));
    }
}
