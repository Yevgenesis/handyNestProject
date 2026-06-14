package com.handynest.notification;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.handynest.identity.UserRepository;
import com.handynest.platform.PlatformSettingKey;
import com.handynest.platform.PlatformSettingService;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OutboxEventWorker {

  private static final int MAX_ATTEMPTS = 5;

  private final OutboxEventRepository outboxEventRepository;
  private final NotificationRepository notificationRepository;
  private final ObjectMapper objectMapper;
  private final UserRepository userRepository;
  private final PlatformSettingService platformSettingService;

  @Value("${app.notifications.outbox-worker.batch-size:50}")
  private int batchSize;

  @Scheduled(
      fixedDelayString = "${app.notifications.outbox-worker.fixed-delay:10s}",
      initialDelayString = "${app.notifications.outbox-worker.initial-delay:10s}")
  @Transactional
  public void processDueEvents() {
    List<OutboxEvent> events =
        outboxEventRepository.findDueEvents(
            List.of(OutboxStatus.PENDING.name(), OutboxStatus.FAILED.name()),
            Instant.now(),
            PageRequest.of(0, Math.max(1, batchSize)));
    events.forEach(this::processEvent);
  }

  private void processEvent(OutboxEvent event) {
    event.markProcessing();
    try {
      JsonNode payload = objectMapper.readTree(event.getPayloadJson());
      String recipientUserId = payload.path("recipientUserId").asText(null);
      String notificationType = payload.path("notificationType").asText(null);
      if (payload.path("recipientUserIds").isArray() && notificationType != null) {
        for (JsonNode recipient : payload.path("recipientUserIds")) {
          createNotification(event, payload, recipient.asText(), notificationType);
        }
      } else if (recipientUserId != null && notificationType != null) {
        createNotification(event, payload, recipientUserId, notificationType);
      }
      Instant now = Instant.now();
      event.markProcessed(now);
    } catch (Exception exception) {
      boolean deadLetter = event.getAttempts() >= MAX_ATTEMPTS;
      Duration baseDelay =
          platformSettingService.durationValue(PlatformSettingKey.OUTBOX_RETRY_BASE_DELAY);
      long multiplier = Math.min(1L << Math.max(0, event.getAttempts() - 1), 60L);
      event.markFailed(
          trimError(exception), Instant.now().plus(baseDelay.multipliedBy(multiplier)), deadLetter);
    }
  }

  private void createNotification(
      OutboxEvent event, JsonNode payload, String recipientUserId, String notificationType)
      throws Exception {
    com.handynest.identity.User user =
        userRepository
            .findByPublicId(recipientUserId)
            .orElseThrow(() -> new IllegalStateException("Notification recipient not found"));
    if (notificationRepository.existsBySourceEventIdAndUserId(event.getId(), user.getId())) {
      return;
    }
    Notification notification =
        new Notification(
            event.getId(),
            user,
            NotificationType.valueOf(notificationType),
            requiredText(payload, "title"),
            requiredText(payload, "body"),
            requiredText(payload, "targetType"),
            requiredText(payload, "targetId"),
            objectMapper.writeValueAsString(payload.path("metadata")));
    notification.markDelivered(Instant.now());
    notificationRepository.save(notification);
  }

  private String requiredText(JsonNode payload, String field) {
    String value = payload.path(field).asText(null);
    if (value == null || value.isBlank()) {
      throw new IllegalStateException(field + " is required");
    }
    return value;
  }

  private String trimError(Exception exception) {
    String message = exception.getMessage();
    if (message == null || message.isBlank()) {
      message = exception.getClass().getSimpleName();
    }
    return message.length() <= 1000 ? message : message.substring(0, 1000);
  }
}
