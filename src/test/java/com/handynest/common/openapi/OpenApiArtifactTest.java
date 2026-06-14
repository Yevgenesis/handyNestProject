package com.handynest.common.openapi;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.handynest.HandyNestProjectApplication;
import com.handynest.testsupport.TestDatabaseConfig;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(classes = {TestDatabaseConfig.class, HandyNestProjectApplication.class})
class OpenApiArtifactTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void writesFrontendContractArtifact() throws Exception {
    byte[] specification =
        mockMvc
            .perform(get("/v3/api-docs"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsByteArray();
    Path output = Path.of("target", "openapi", "openapi.json");
    Files.createDirectories(output.getParent());
    Files.write(output, specification);
  }
}
