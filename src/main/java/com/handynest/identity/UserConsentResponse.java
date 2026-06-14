package com.handynest.identity;

import java.time.Instant;

public record UserConsentResponse(ConsentType type, String documentVersion, Instant acceptedAt) {}
