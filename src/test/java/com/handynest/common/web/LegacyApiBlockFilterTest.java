package com.handynest.common.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.handynest.HandyNestProjectApplication;
import com.handynest.testsupport.TestDatabaseConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(classes = {TestDatabaseConfig.class, HandyNestProjectApplication.class})
class LegacyApiBlockFilterTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ApplicationContext applicationContext;

  @Test
  void legacyRestControllersAreNotRegisteredByDefault() {
    assertThat(applicationContext.containsBean("attachmentController")).isFalse();
    assertThat(applicationContext.containsBean("categoryController")).isFalse();
    assertThat(applicationContext.containsBean("feedbackController")).isFalse();
    assertThat(applicationContext.containsBean("messageController")).isFalse();
    assertThat(applicationContext.containsBean("performerController")).isFalse();
    assertThat(applicationContext.containsBean("taskController")).isFalse();
    assertThat(applicationContext.containsBean("userController")).isFalse();
  }

  @Test
  void legacyRootEndpointsReturnGoneWithUnifiedErrorContract() throws Exception {
    mockMvc
        .perform(get("/categories"))
        .andExpect(status().isGone())
        .andExpect(jsonPath("$.code").value("LEGACY_API_DISABLED"))
        .andExpect(jsonPath("$.message").value("Legacy API is disabled; use /api/v1"))
        .andExpect(jsonPath("$.path").value("/categories"));

    mockMvc
        .perform(get("/tasks/open"))
        .andExpect(status().isGone())
        .andExpect(jsonPath("$.code").value("LEGACY_API_DISABLED"))
        .andExpect(jsonPath("$.path").value("/tasks/open"));
  }

  @Test
  void apiV1EndpointsRemainAvailable() throws Exception {
    mockMvc.perform(get("/api/v1/categories")).andExpect(status().isOk());
  }
}
