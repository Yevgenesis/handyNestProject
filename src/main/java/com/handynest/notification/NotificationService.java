package com.handynest.notification;

import com.handynest.common.api.PageResponse;
import com.handynest.common.error.ResourceNotFoundException;
import com.handynest.identity.User;
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

  private final NotificationRepository notificationRepository;
  private final UserProfileService userProfileService;
  private final DomainEventPublisher domainEventPublisher;

  @Transactional(readOnly = true)
  public PageResponse<NotificationResponse> myNotifications(
      UserDetails userDetails, Boolean unreadOnly, int page, int size) {
    User user = userProfileService.currentUser(userDetails);
    PageRequest pageable =
        PageRequest.of(
            Math.max(page, 0),
            Math.min(Math.max(size, 1), 100),
            Sort.by(Sort.Direction.DESC, "createdAt"));
    if (Boolean.TRUE.equals(unreadOnly)) {
      return PageResponse.from(
          notificationRepository
              .findAllByUserIdAndReadAtIsNullOrderByCreatedAtDesc(user.getId(), pageable)
              .map(this::toResponse));
    }
    return PageResponse.from(
        notificationRepository
            .findAllByUserIdOrderByCreatedAtDesc(user.getId(), pageable)
            .map(this::toResponse));
  }

  @Transactional
  public NotificationResponse markRead(UserDetails userDetails, String notificationId) {
    User user = userProfileService.currentUser(userDetails);
    Notification notification =
        notificationRepository
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
  public void notifyUser(
      User user,
      NotificationType type,
      String title,
      String body,
      String targetType,
      String targetId,
      Map<String, ?> metadata) {
    domainEventPublisher.publish(
        eventType(type),
        targetType,
        targetId,
        user,
        type,
        title,
        body,
        targetType,
        targetId,
        metadata);
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
        notification.getCreatedAt());
  }

  private DomainEventType eventType(NotificationType type) {
    return switch (type) {
      case USER_REGISTERED -> DomainEventType.USER_REGISTERED;
      case TASK_CREATED -> DomainEventType.TASK_CREATED;
      case TASK_PUBLISHED -> DomainEventType.TASK_PUBLISHED;
      case TASK_REJECTED -> DomainEventType.TASK_REJECTED;
      case TASK_EXPIRED -> DomainEventType.TASK_EXPIRED;
      case NEW_OFFER -> DomainEventType.OFFER_CREATED;
      case OFFER_ACCEPTED -> DomainEventType.OFFER_ACCEPTED;
      case OFFER_REJECTED -> DomainEventType.OFFER_REJECTED;
      case OFFER_EXPIRED -> DomainEventType.OFFER_EXPIRED;
      case DEAL_CREATED -> DomainEventType.DEAL_CREATED;
      case CONTACT_REVEALED -> DomainEventType.CONTACT_REVEALED;
      case DEAL_CANCELED -> DomainEventType.DEAL_CANCELED;
      case NEW_CHAT_MESSAGE -> DomainEventType.CHAT_MESSAGE_CREATED;
      case WORK_SUBMITTED -> DomainEventType.WORK_SUBMITTED;
      case WORK_ACCEPTED -> DomainEventType.WORK_ACCEPTED;
      case REVISION_REQUESTED -> DomainEventType.REVISION_REQUESTED;
      case DISPUTE_OPENED -> DomainEventType.DISPUTE_OPENED;
      case DISPUTE_RESOLVED -> DomainEventType.DISPUTE_RESOLVED;
      case FEEDBACK_CREATED -> DomainEventType.FEEDBACK_CREATED;
      case VERIFICATION_APPROVED -> DomainEventType.VERIFICATION_APPROVED;
      case VERIFICATION_REJECTED -> DomainEventType.VERIFICATION_REJECTED;
      case PAYMENT_EVENT -> DomainEventType.PAYMENT_PENDING;
      case USER_BLOCKED -> DomainEventType.USER_BLOCKED;
    };
  }
}
