package com.handynest.common.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.handynest.identity.User;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditLogService {

  private final AuditLogRepository repository;
  private final ObjectMapper objectMapper;

  public void append(
      User actor,
      String action,
      String entityType,
      String entityPublicId,
      Map<String, ?> metadata,
      String ipAddress,
      String userAgent) {
    repository.save(
        new AuditLog(
            actor,
            action,
            entityType,
            entityPublicId,
            toJson(metadata == null ? Map.of() : metadata),
            ipAddress,
            userAgent));
  }

  private String toJson(Map<String, ?> metadata) {
    try {
      return objectMapper.writeValueAsString(metadata);
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Failed to serialize audit metadata", exception);
    }
  }
}
