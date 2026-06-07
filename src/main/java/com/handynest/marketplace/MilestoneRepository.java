package com.handynest.marketplace;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MilestoneRepository extends JpaRepository<Milestone, Long> {

    @EntityGraph(attributePaths = {
            "deal",
            "deal.task",
            "deal.customer",
            "deal.performer",
            "deal.performer.user",
            "task"
    })
    List<Milestone> findAllByDealPublicIdOrderByCreatedAtAsc(String dealPublicId);

    @EntityGraph(attributePaths = {
            "deal",
            "deal.task",
            "deal.customer",
            "deal.performer",
            "deal.performer.user",
            "task"
    })
    Optional<Milestone> findByPublicId(String publicId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {
            "deal",
            "deal.task",
            "deal.customer",
            "deal.performer",
            "deal.performer.user",
            "task"
    })
    @Query("select milestone from Milestone milestone where milestone.publicId = :publicId")
    Optional<Milestone> findByPublicIdForUpdate(@Param("publicId") String publicId);

    boolean existsByDealIdAndStatusNot(Long dealId, MilestoneStatus status);
}
