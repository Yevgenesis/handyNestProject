package com.handynest.catalog.category;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Earliest deal stage at which participant contacts may be revealed.")
public enum ContactRevealStage {
  AFTER_DEAL_CREATED,
  AFTER_WORK_STARTED,
  NEVER
}
