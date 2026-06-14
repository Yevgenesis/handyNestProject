package com.handynest.marketplace;

import java.time.Instant;

public interface DealChatSummaryProjection {

  String getPublicId();

  String getDealId();

  String getTaskId();

  String getTaskTitle();

  String getCustomerId();

  String getCustomerDisplayName();

  String getPerformerId();

  String getPerformerDisplayName();

  String getParticipantRole();

  String getStatus();

  String getLastMessageText();

  String getLastMessageType();

  Instant getLastMessageAt();

  long getUnreadCount();

  Instant getClosedAt();

  Instant getCreatedAt();

  Instant getUpdatedAt();
}
