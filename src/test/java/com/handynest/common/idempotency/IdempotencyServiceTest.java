package com.handynest.common.idempotency;

import com.handynest.HandyNestProjectApplication;
import com.handynest.testsupport.TestDatabaseConfig;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@ContextConfiguration(classes = {TestDatabaseConfig.class, HandyNestProjectApplication.class})
class IdempotencyServiceTest {

    @Autowired
    private IdempotencyService idempotencyService;

    @Autowired
    private IdempotencyKeyRepository idempotencyKeyRepository;

    @Test
    void cleanupExpiredKeysDeletesOnlyExpiredRows() {
        String suffix = UUID.randomUUID().toString();
        String endpoint = "/api/v1/test/" + suffix;
        IdempotencyKey expired = idempotencyKeyRepository.save(new IdempotencyKey(
                null,
                "expired-" + suffix,
                idempotencyService.sha256("expired"),
                endpoint,
                Instant.now().minus(1, ChronoUnit.MINUTES)
        ));
        IdempotencyKey active = idempotencyKeyRepository.save(new IdempotencyKey(
                null,
                "active-" + suffix,
                idempotencyService.sha256("active"),
                endpoint,
                Instant.now().plus(1, ChronoUnit.HOURS)
        ));

        long deleted = idempotencyService.cleanupExpiredKeys(Instant.now());

        assertThat(deleted).isEqualTo(1);
        assertThat(idempotencyKeyRepository.findById(expired.getId())).isEmpty();
        assertThat(idempotencyKeyRepository.findById(active.getId())).isPresent();
    }
}
