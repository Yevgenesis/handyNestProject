package com.handynest.notification;

import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

  @Query(
      value =
          """
            select *
              from outbox_event
             where status in (:statuses)
               and next_attempt_at <= :nextAttemptAt
             order by created_at asc
             for update skip locked
            """,
      nativeQuery = true)
  List<OutboxEvent> findDueEvents(
      @Param("statuses") List<String> statuses,
      @Param("nextAttemptAt") Instant nextAttemptAt,
      Pageable pageable);
}
