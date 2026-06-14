package com.handynest.marketplace;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DisputeCaseRepository extends JpaRepository<DisputeCase, Long> {

  boolean existsByDealId(Long dealId);

  @EntityGraph(
      attributePaths = {
        "deal",
        "deal.customer",
        "deal.performer",
        "deal.performer.user",
        "task",
        "openedByUser"
      })
  Optional<DisputeCase> findByPublicId(String publicId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @EntityGraph(
      attributePaths = {
        "deal",
        "deal.customer",
        "deal.performer",
        "deal.performer.user",
        "task",
        "openedByUser"
      })
  @Query("select disputeCase from DisputeCase disputeCase where disputeCase.publicId = :publicId")
  Optional<DisputeCase> findByPublicIdForUpdate(@Param("publicId") String publicId);

  @EntityGraph(
      attributePaths = {
        "deal",
        "deal.customer",
        "deal.performer",
        "deal.performer.user",
        "task",
        "openedByUser"
      })
  Page<DisputeCase> findAllByDealCustomerIdOrDealPerformerUserIdOrderByCreatedAtDesc(
      Long customerId, Long performerUserId, Pageable pageable);

  @EntityGraph(
      attributePaths = {
        "deal",
        "deal.customer",
        "deal.performer",
        "deal.performer.user",
        "task",
        "openedByUser"
      })
  Page<DisputeCase> findAllByStatusOrderByCreatedAtAsc(DisputeCaseStatus status, Pageable pageable);
}
