package com.handynest.marketplace;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DealRepository extends JpaRepository<Deal, Long> {

    boolean existsByTaskId(Long taskId);

    long countByTaskPublicId(String taskPublicId);

    @EntityGraph(attributePaths = {
            "task",
            "task.category",
            "task.city",
            "customer",
            "performer",
            "performer.user",
            "acceptedOffer"
    })
    Optional<Deal> findByPublicId(String publicId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {
            "task",
            "task.category",
            "task.city",
            "customer",
            "performer",
            "performer.user",
            "acceptedOffer"
    })
    @Query("select deal from Deal deal where deal.publicId = :publicId")
    Optional<Deal> findByPublicIdForUpdate(@Param("publicId") String publicId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {
            "task",
            "task.category",
            "task.city",
            "customer",
            "performer",
            "performer.user",
            "acceptedOffer"
    })
    @Query("select deal from Deal deal where deal.task.publicId = :taskPublicId")
    Optional<Deal> findByTaskPublicIdForUpdate(@Param("taskPublicId") String taskPublicId);

    @EntityGraph(attributePaths = {
            "task",
            "task.category",
            "task.city",
            "customer",
            "performer",
            "performer.user",
            "acceptedOffer"
    })
    List<Deal> findAllByCustomerIdOrPerformerUserIdOrderByCreatedAtDesc(Long customerId, Long performerUserId);
}
