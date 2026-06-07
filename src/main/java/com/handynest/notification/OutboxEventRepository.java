package com.handynest.notification;

import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    @Query("""
            select event
              from OutboxEvent event
             where event.status in (:statuses)
               and event.nextAttemptAt <= :nextAttemptAt
             order by event.createdAt asc
            """)
    List<OutboxEvent> findDueEvents(
            @Param("statuses") List<OutboxStatus> statuses,
            @Param("nextAttemptAt") Instant nextAttemptAt,
            Pageable pageable);
}
