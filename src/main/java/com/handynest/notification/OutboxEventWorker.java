package com.handynest.notification;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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

    private static final String IN_APP_NOTIFICATION_CREATED = "IN_APP_NOTIFICATION_CREATED";
    private static final int MAX_ATTEMPTS = 5;

    private final OutboxEventRepository outboxEventRepository;
    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.notifications.outbox-worker.batch-size:50}")
    private int batchSize;

    @Scheduled(fixedDelayString = "${app.notifications.outbox-worker.fixed-delay:10s}")
    @Transactional
    public void processDueEvents() {
        List<OutboxEvent> events = outboxEventRepository.findDueEvents(
                List.of(OutboxStatus.PENDING, OutboxStatus.FAILED),
                Instant.now(),
                PageRequest.of(0, Math.max(1, batchSize))
        );
        events.forEach(this::processEvent);
    }

    private void processEvent(OutboxEvent event) {
        event.markProcessing();
        try {
            if (!IN_APP_NOTIFICATION_CREATED.equals(event.getEventType())) {
                throw new IllegalStateException("Unsupported outbox event type: " + event.getEventType());
            }
            JsonNode payload = objectMapper.readTree(event.getPayloadJson());
            String notificationId = payload.path("notificationId").asText(null);
            if (notificationId == null || notificationId.isBlank()) {
                throw new IllegalStateException("notificationId is required");
            }

            Notification notification = notificationRepository.findByPublicId(notificationId)
                    .orElseThrow(() -> new IllegalStateException("Notification not found: " + notificationId));
            Instant now = Instant.now();
            notification.markDelivered(now);
            event.markProcessed(now);
        } catch (Exception exception) {
            boolean deadLetter = event.getAttempts() >= MAX_ATTEMPTS;
            event.markFailed(trimError(exception), Instant.now().plus(1, ChronoUnit.MINUTES), deadLetter);
        }
    }

    private String trimError(Exception exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            message = exception.getClass().getSimpleName();
        }
        return message.length() <= 1000 ? message : message.substring(0, 1000);
    }
}
