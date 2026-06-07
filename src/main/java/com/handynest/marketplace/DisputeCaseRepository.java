package com.handynest.marketplace;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DisputeCaseRepository extends JpaRepository<DisputeCase, Long> {

    boolean existsByDealId(Long dealId);

    @EntityGraph(attributePaths = {
            "deal",
            "deal.customer",
            "deal.performer",
            "deal.performer.user",
            "task",
            "openedByUser"
    })
    Optional<DisputeCase> findByPublicId(String publicId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {
            "deal",
            "deal.customer",
            "deal.performer",
            "deal.performer.user",
            "task",
            "openedByUser"
    })
    @Query("select disputeCase from DisputeCase disputeCase where disputeCase.publicId = :publicId")
    Optional<DisputeCase> findByPublicIdForUpdate(@Param("publicId") String publicId);

    @EntityGraph(attributePaths = {
            "deal",
            "deal.customer",
            "deal.performer",
            "deal.performer.user",
            "task",
            "openedByUser"
    })
    List<DisputeCase> findAllByDealCustomerIdOrDealPerformerUserIdOrderByCreatedAtDesc(
            Long customerId,
            Long performerUserId
    );

    @EntityGraph(attributePaths = {
            "deal",
            "deal.customer",
            "deal.performer",
            "deal.performer.user",
            "task",
            "openedByUser"
    })
    List<DisputeCase> findAllByStatusOrderByCreatedAtAsc(DisputeCaseStatus status);
}
