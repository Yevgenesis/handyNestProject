package com.handynest.marketplace;

import java.time.Instant;

public record ContactRevealHistoryResponse(
    String publicId,
    String dealId,
    String requestedByUserId,
    String subjectUserId,
    ContactType contactType,
    Instant revealedAt) {}
