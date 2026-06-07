package com.handynest.marketplace;

import jakarta.validation.constraints.Size;

public record TaskModerationRejectRequest(
        @Size(max = 1000) String reason
) {
}
