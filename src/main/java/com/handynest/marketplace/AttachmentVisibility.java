package com.handynest.marketplace;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Attachment visibility and access policy.")
public enum AttachmentVisibility {
  PUBLIC,
  PRIVATE,
  ADMIN_ONLY,
  PARTICIPANTS_ONLY
}
