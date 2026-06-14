package com.handynest.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.handynest.identity.User;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DomainEventPublisher {

  private final OutboxEventRepository outboxEventRepository;
  private final ObjectMapper objectMapper;

  public OutboxEvent publish(
      DomainEventType eventType,
      String aggregateType,
      String aggregateId,
      User recipient,
      NotificationType notificationType,
      String title,
      String body,
      String targetType,
      String targetId,
      Map<String, ?> metadata) {
    Map<String, Object> payload = new LinkedHashMap<>();
    if (recipient != null) {
      payload.put("recipientUserId", recipient.getPublicId());
    }
    if (notificationType != null) {
      payload.put("notificationType", notificationType.name());
      payload.put("title", title);
      payload.put("body", body);
      payload.put("targetType", targetType);
      payload.put("targetId", targetId);
    }
    payload.put("metadata", metadata == null ? Map.of() : metadata);
    payload.put("occurredAt", Instant.now().toString());

    return outboxEventRepository.save(
        new OutboxEvent(
            eventType.name(), aggregateType, aggregateId, toJson(payload), Instant.now()));
  }

  public OutboxEvent publish(DomainEventType eventType, String aggregateType, String aggregateId) {
    return publish(
        eventType, aggregateType, aggregateId, null, null, null, null, null, null, Map.of());
  }

  public OutboxEvent publishToUsers(
      DomainEventType eventType,
      String aggregateType,
      String aggregateId,
      List<User> recipients,
      NotificationType notificationType,
      String title,
      String body,
      String targetType,
      String targetId,
      Map<String, ?> metadata) {
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("recipientUserIds", recipients.stream().map(User::getPublicId).distinct().toList());
    payload.put("notificationType", notificationType.name());
    payload.put("title", title);
    payload.put("body", body);
    payload.put("targetType", targetType);
    payload.put("targetId", targetId);
    payload.put("metadata", metadata == null ? Map.of() : metadata);
    payload.put("occurredAt", Instant.now().toString());
    return outboxEventRepository.save(
        new OutboxEvent(
            eventType.name(), aggregateType, aggregateId, toJson(payload), Instant.now()));
  }

  private String toJson(Map<String, ?> value) {
    try {
      return objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Failed to serialize domain event payload", exception);
    }
  }
}
