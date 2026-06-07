package com.handynest.risk;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RiskEventRepository extends JpaRepository<RiskEvent, Long> {

    @EntityGraph(attributePaths = {
            "user",
            "task",
            "chat",
            "chatMessage",
            "resolvedByAdmin"
    })
    Optional<RiskEvent> findByPublicId(String publicId);

    @EntityGraph(attributePaths = {
            "user",
            "task",
            "chat",
            "chatMessage",
            "resolvedByAdmin"
    })
    List<RiskEvent> findAllByStatusOrderByCreatedAtDesc(RiskEventStatus status);

    @EntityGraph(attributePaths = {
            "user",
            "task",
            "chat",
            "chatMessage",
            "resolvedByAdmin"
    })
    List<RiskEvent> findAllByRiskTypeAndStatusOrderByCreatedAtDesc(RiskType riskType, RiskEventStatus status);

    @EntityGraph(attributePaths = {
            "user",
            "task",
            "chat",
            "chatMessage",
            "resolvedByAdmin"
    })
    List<RiskEvent> findAllBySeverityAndStatusOrderByCreatedAtDesc(RiskSeverity severity, RiskEventStatus status);
}
