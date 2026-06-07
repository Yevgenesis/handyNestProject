package com.handynest.moderation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ComplaintCreateRequest(
        @NotNull ModerationTargetType targetType,
        @NotBlank @Size(max = 64) String targetId,
        @Size(max = 26) String targetUserId,
        @NotBlank @Size(max = 500) String reason,
        @NotBlank @Size(max = 4000) String description
) {
}
