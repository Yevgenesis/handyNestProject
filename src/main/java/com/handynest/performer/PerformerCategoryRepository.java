package com.handynest.performer;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerformerCategoryRepository extends JpaRepository<PerformerCategory, Long> {

  @EntityGraph(
      attributePaths = {"performerProfile", "performerProfile.user", "category", "reviewedBy"})
  Optional<PerformerCategory> findByPerformerProfilePublicIdAndCategoryPublicId(
      String performerId, String categoryId);

  @EntityGraph(
      attributePaths = {"performerProfile", "performerProfile.user", "category", "reviewedBy"})
  List<PerformerCategory> findAllByApprovalStatusOrderByCreatedAtAsc(
      PerformerCategoryApprovalStatus approvalStatus);

  List<PerformerCategory> findAllByCategoryId(Long categoryId);
}
