package com.handynest.moderation;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ModerationCaseRepository extends JpaRepository<ModerationCase, Long> {

  @EntityGraph(attributePaths = {"openedByUser", "assignedAdmin"})
  Optional<ModerationCase> findByPublicId(String publicId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @EntityGraph(attributePaths = {"openedByUser", "assignedAdmin"})
  @Query(
      "select moderationCase from ModerationCase moderationCase where moderationCase.publicId = :publicId")
  Optional<ModerationCase> findByPublicIdForUpdate(@Param("publicId") String publicId);

  @EntityGraph(attributePaths = {"openedByUser", "assignedAdmin"})
  Page<ModerationCase> findAllByStatusOrderByCreatedAtAsc(
      ModerationCaseStatus status, Pageable pageable);

  @EntityGraph(attributePaths = {"openedByUser", "assignedAdmin"})
  Page<ModerationCase> findAllByTargetTypeAndStatusOrderByCreatedAtAsc(
      ModerationTargetType targetType, ModerationCaseStatus status, Pageable pageable);

  boolean existsByTargetTypeAndTargetIdAndStatusIn(
      ModerationTargetType targetType, String targetId, List<ModerationCaseStatus> statuses);
}
