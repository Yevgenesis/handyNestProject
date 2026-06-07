package com.handynest.notification;

import com.handynest.identity.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.handynest.common.api.PageResponse;
import com.handynest.common.error.ResourceNotFoundException;
import com.handynest.identity.UserProfileService;
import java.time.Instant;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final String IN_APP_NOTIFICATION_CREATED = "IN_APP_NOTIFICATION_CREATED";

    private final NotificationRepository notificationRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final UserProfileService userProfileService;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> myNotifications(
            UserDetails userDetails,
            Boolean unreadOnly,
            int page,
            int size
    ) {
        User user = userProfileService.currentUser(userDetails);
        PageRequest pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        if (Boolean.TRUE.equals(unreadOnly)) {
            return PageResponse.from(notificationRepository
                    .findAllByUserIdAndReadAtIsNullOrderByCreatedAtDesc(user.getId(), pageable)
                    .map(this::toResponse));
        }
        return PageResponse.from(notificationRepository
                .findAllByUserIdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(this::toResponse));
    }

    @Transactional
    public NotificationResponse markRead(UserDetails userDetails, String notificationId) {
        User user = userProfileService.currentUser(userDetails);
        Notification notification = notificationRepository
                .findByPublicIdAndUserId(notificationId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Notification", notificationId));
        notification.markRead(Instant.now());
        return toResponse(notification);
    }

    @Transactional
    public int markAllRead(UserDetails userDetails) {
        User user = userProfileService.currentUser(userDetails);
        return notificationRepository.markAllUnreadAsRead(user.getId(), Instant.now());
    }

    @Transactional
    public Notification notifyUser(
            User user,
            NotificationType type,
            String title,
            String body,
            String targetType,
            String targetId,
            Map<String, ?> metadata
    ) {
        Notification notification = notificationRepository.save(new Notification(
                user,
                type,
                title,
                body,
                targetType,
                targetId,
                toJson(metadata == null ? Map.of() : metadata)
        ));
        outboxEventRepository.save(new OutboxEvent(
                IN_APP_NOTIFICATION_CREATED,
                "Notification",
                notification.getPublicId(),
                toJson(Map.of(
                        "notificationId", notification.getPublicId(),
                        "userId", user.getPublicId(),
                        "type", type.name(),
                        "targetType", targetType,
                        "targetId", targetId
                )),
                Instant.now()
        ));
        return notification;
    }

    public NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getPublicId(),
                notification.getType().name(),
                notification.getTitle(),
                notification.getBody(),
                notification.getTargetType(),
                notification.getTargetId(),
                notification.getReadAt(),
                notification.getDeliveredAt(),
                notification.getMetadataJson(),
                notification.getCreatedAt()
        );
    }

    private String toJson(Map<String, ?> value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize notification payload", exception);
        }
    }
}
