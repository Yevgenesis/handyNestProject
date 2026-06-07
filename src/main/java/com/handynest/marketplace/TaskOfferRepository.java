package com.handynest.marketplace;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskOfferRepository extends JpaRepository<TaskOffer, Long> {

    boolean existsByTaskIdAndPerformerIdAndStatus(Long taskId, Long performerId, TaskOfferStatus status);

    long countByTaskPublicIdAndStatus(String taskPublicId, TaskOfferStatus status);

    @EntityGraph(attributePaths = {
            "task",
            "task.customer",
            "performer",
            "performer.user"
    })
    List<TaskOffer> findAllByTaskPublicIdOrderByCreatedAtDesc(String taskPublicId);

    @EntityGraph(attributePaths = {
            "task",
            "task.customer",
            "performer",
            "performer.user"
    })
    List<TaskOffer> findAllByPerformerUserIdOrderByCreatedAtDesc(Long userId);

    @EntityGraph(attributePaths = {
            "task",
            "task.customer",
            "performer",
            "performer.user"
    })
    Optional<TaskOffer> findByPublicId(String publicId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select offer
            from TaskOffer offer
            join fetch offer.task task
            join fetch task.customer
            join fetch offer.performer performer
            join fetch performer.user
            where task.publicId = :taskPublicId and offer.publicId = :offerPublicId
            """)
    Optional<TaskOffer> findByTaskPublicIdAndPublicIdForUpdate(
            @Param("taskPublicId") String taskPublicId,
            @Param("offerPublicId") String offerPublicId
    );

    List<TaskOffer> findAllByTaskIdAndStatus(Long taskId, TaskOfferStatus status);

    List<TaskOffer> findAllByStatusAndExpiresAtLessThanEqual(
            TaskOfferStatus status,
            Instant expiresAt,
            Pageable pageable
    );
}
