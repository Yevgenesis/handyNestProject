package com.handynest.marketplace;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DealChatRepository extends JpaRepository<DealChat, Long> {

    long countByTaskPublicId(String taskPublicId);

    @EntityGraph(attributePaths = {
            "deal",
            "task",
            "customer",
            "performer",
            "performer.user"
    })
    Optional<DealChat> findByDealId(Long dealId);

    @EntityGraph(attributePaths = {
            "deal",
            "deal.acceptedOffer",
            "task",
            "task.customer",
            "customer",
            "performer",
            "performer.user"
    })
    Optional<DealChat> findByPublicId(String publicId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select chat from DealChat chat
            join fetch chat.deal deal
            join fetch deal.acceptedOffer
            join fetch chat.task task
            join fetch task.customer
            join fetch chat.customer
            join fetch chat.performer performer
            join fetch performer.user
            where chat.publicId = :publicId
            """)
    Optional<DealChat> findByPublicIdForUpdate(@Param("publicId") String publicId);

    @EntityGraph(attributePaths = {
            "deal",
            "task",
            "customer",
            "performer",
            "performer.user"
    })
    List<DealChat> findAllByCustomerIdOrPerformerUserIdOrderByCreatedAtDesc(Long customerId, Long performerUserId);
}
