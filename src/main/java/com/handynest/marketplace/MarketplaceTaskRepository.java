package com.handynest.marketplace;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public interface MarketplaceTaskRepository
    extends JpaRepository<MarketplaceTask, Long>, JpaSpecificationExecutor<MarketplaceTask> {

  @EntityGraph(
      attributePaths = {
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

  @Override
  @EntityGraph(
      attributePaths = {
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
  @NonNull
  Page<MarketplaceTask> findAll(
      @Nullable Specification<MarketplaceTask> specification, @NonNull Pageable pageable);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select task from MarketplaceTask task where task.publicId = :publicId")
  Optional<MarketplaceTask> findByPublicIdForUpdate(@Param("publicId") String publicId);

  List<MarketplaceTask> findAllByStatusAndExpiresAtLessThanEqual(
      TaskStatus status, Instant expiresAt, Pageable pageable);

  long countByCustomerIdAndStatusIn(Long customerId, List<TaskStatus> statuses);

  long countByCustomerIdAndStatusAndCanceledAtAfter(
      Long customerId, TaskStatus status, Instant canceledAfter);
}
