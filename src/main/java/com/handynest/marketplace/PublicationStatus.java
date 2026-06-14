package com.handynest.marketplace;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Task publication and moderation visibility status.")
public enum PublicationStatus {
  UNPUBLISHED,
  PUBLISHED,
  HIDDEN_BY_ADMIN,
  REJECTED_BY_MODERATION
}
