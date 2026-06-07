package com.handynest.moderation;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    @EntityGraph(attributePaths = {"reporter", "targetUser", "moderationCase"})
    Optional<Complaint> findByPublicId(String publicId);

    @EntityGraph(attributePaths = {"reporter", "targetUser", "moderationCase"})
    List<Complaint> findAllByReporterIdOrderByCreatedAtDesc(Long reporterId);

    @EntityGraph(attributePaths = {"reporter", "targetUser", "moderationCase"})
    List<Complaint> findAllByModerationCaseId(Long moderationCaseId);
}
