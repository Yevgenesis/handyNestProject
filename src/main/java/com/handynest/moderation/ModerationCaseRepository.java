package com.handynest.moderation;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModerationCaseRepository extends JpaRepository<ModerationCase, Long> {

    @EntityGraph(attributePaths = {"openedByUser", "assignedAdmin"})
    Optional<ModerationCase> findByPublicId(String publicId);

    @EntityGraph(attributePaths = {"openedByUser", "assignedAdmin"})
    List<ModerationCase> findAllByStatusOrderByCreatedAtAsc(ModerationCaseStatus status);

    @EntityGraph(attributePaths = {"openedByUser", "assignedAdmin"})
    List<ModerationCase> findAllByTargetTypeAndStatusOrderByCreatedAtAsc(
            ModerationTargetType targetType,
            ModerationCaseStatus status
    );
}
