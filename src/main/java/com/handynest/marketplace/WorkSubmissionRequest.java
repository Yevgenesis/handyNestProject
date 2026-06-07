package com.handynest.marketplace;

import jakarta.validation.constraints.Size;

public record WorkSubmissionRequest(
        @Size(max = 2000) String message
) {
}
