package com.handynest.common.idempotency;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, Long> {

    Optional<IdempotencyKey> findByUserIdAndKeyAndTargetEndpoint(Long userId, String key, String targetEndpoint);

    Optional<IdempotencyKey> findByUserIsNullAndKeyAndTargetEndpoint(String key, String targetEndpoint);

    long deleteByExpiresAtBefore(java.time.Instant now);
}
