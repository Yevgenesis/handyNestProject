package com.handynest.notification;

import java.time.Instant;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

  boolean existsBySourceEventIdAndUserId(Long sourceEventId, Long userId);

  Page<Notification> findAllByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

  Page<Notification> findAllByUserIdAndReadAtIsNullOrderByCreatedAtDesc(
      Long userId, Pageable pageable);

  Optional<Notification> findByPublicIdAndUserId(String publicId, Long userId);

  Optional<Notification> findByPublicId(String publicId);

  @Modifying
  @Query(
      """
            update Notification notification
               set notification.readAt = :readAt
             where notification.user.id = :userId
               and notification.readAt is null
            """)
  int markAllUnreadAsRead(@Param("userId") Long userId, @Param("readAt") Instant readAt);
}
