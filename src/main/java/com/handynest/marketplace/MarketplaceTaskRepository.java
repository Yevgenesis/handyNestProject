package com.handynest.marketplace;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MarketplaceTaskRepository
        extends JpaRepository<MarketplaceTask, Long>, JpaSpecificationExecutor<MarketplaceTask> {

    @EntityGraph(attributePaths = {
            "customer",
            "category",
            "country",
            "region",
            "city",
            "district",
            "selectedOffer",
            "selectedPerformer",
            "selectedPerformer.user"
    })
    Optional<MarketplaceTask> findByPublicId(String publicId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select task from MarketplaceTask task where task.publicId = :publicId")
    Optional<MarketplaceTask> findByPublicIdForUpdate(@Param("publicId") String publicId);

    List<MarketplaceTask> findAllByStatusAndExpiresAtLessThanEqual(
            TaskStatus status,
            Instant expiresAt,
            Pageable pageable
    );
}
