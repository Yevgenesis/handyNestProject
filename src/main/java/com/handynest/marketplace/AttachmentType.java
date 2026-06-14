package com.handynest.marketplace;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Marketplace attachment business type.")
public enum AttachmentType {
  TASK_IMAGE,
  CHAT_FILE,
  WORK_RESULT,
  DISPUTE_EVIDENCE,
  VERIFICATION_DOCUMENT,
  AVATAR,
  PORTFOLIO
}
