package com.handynest.payment;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    @EntityGraph(attributePaths = {"deal", "task", "customer", "performer", "performer.user"})
    List<PaymentTransaction> findAllByDealPublicIdOrderByCreatedAtDesc(String dealId);

    @EntityGraph(attributePaths = {"deal", "task", "customer", "performer", "performer.user"})
    Optional<PaymentTransaction> findByIdempotencyKeyAndCustomerId(String idempotencyKey, Long customerId);

    @EntityGraph(attributePaths = {"deal", "task", "customer", "performer", "performer.user"})
    Optional<PaymentTransaction> findByPublicId(String publicId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"deal", "task", "customer", "performer", "performer.user"})
    @Query("""
            select transaction
              from PaymentTransaction transaction
             where transaction.publicId = :publicId
            """)
    Optional<PaymentTransaction> findByPublicIdForUpdate(@Param("publicId") String publicId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"deal", "task", "customer", "performer", "performer.user"})
    @Query("""
            select transaction
              from PaymentTransaction transaction
             where transaction.pspProvider = :provider
               and transaction.pspPaymentId = :pspPaymentId
            """)
    Optional<PaymentTransaction> findByProviderPaymentIdForUpdate(
            @Param("provider") String provider,
            @Param("pspPaymentId") String pspPaymentId);
}
