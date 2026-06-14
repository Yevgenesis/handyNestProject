package com.handynest.marketplace;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record ContactRevealRequest(
    @NotNull ContactType contactType,
    @Schema(description = "Participant publicId; only support roles may choose it explicitly.")
        String participantId) {}
