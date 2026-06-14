package com.handynest.marketplace;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

public record ContactRevealResponse(
    String publicId,
    String dealId,
    String requestedByUserId,
    String subjectUserId,
    ContactType contactType,
    @Schema(
            description =
                "Returned only by the authorized reveal POST; never persisted in history.")
        String contactValue,
    Instant revealedAt) {}
