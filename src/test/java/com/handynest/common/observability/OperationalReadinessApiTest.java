package com.handynest.common.observability;

import com.handynest.HandyNestProjectApplication;
import com.handynest.testsupport.TestDatabaseConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(classes = {TestDatabaseConfig.class, HandyNestProjectApplication.class})
class OperationalReadinessApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthEndpointIsPublicAndReturnsStatus() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void requestCorrelationHeadersAreGenerated() throws Exception {
        mockMvc.perform(get("/api/v1/geo/countries"))
                .andExpect(status().isOk())
                .andExpect(header().exists(RequestCorrelationFilter.REQUEST_ID_HEADER))
                .andExpect(header().exists(RequestCorrelationFilter.CORRELATION_ID_HEADER));
    }

    @Test
    void requestCorrelationHeadersArePropagated() throws Exception {
        mockMvc.perform(get("/api/v1/geo/countries")
                        .header(RequestCorrelationFilter.REQUEST_ID_HEADER, "test-request-id")
                        .header(RequestCorrelationFilter.CORRELATION_ID_HEADER, "test-correlation-id"))
                .andExpect(status().isOk())
                .andExpect(header().string(RequestCorrelationFilter.REQUEST_ID_HEADER, "test-request-id"))
                .andExpect(header().string(RequestCorrelationFilter.CORRELATION_ID_HEADER, "test-correlation-id"));
    }
}
