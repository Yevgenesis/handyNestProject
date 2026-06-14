package com.handynest.marketplace;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoritePerformerRepository extends JpaRepository<FavoritePerformer, Long> {

  boolean existsByCustomerIdAndPerformerProfileId(Long customerId, Long performerProfileId);

  @EntityGraph(
      attributePaths = {"performerProfile", "performerProfile.user", "performerProfile.baseCity"})
  List<FavoritePerformer> findAllByCustomerIdOrderByCreatedAtDesc(Long customerId);

  Optional<FavoritePerformer> findByCustomerIdAndPerformerProfilePublicId(
      Long customerId, String performerId);
}
