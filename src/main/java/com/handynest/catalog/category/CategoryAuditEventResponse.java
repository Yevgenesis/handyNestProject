package com.handynest.catalog.category;

import java.time.Instant;

public record CategoryAuditEventResponse(
    CategoryAuditAction action, String actorUserId, String changedFields, Instant createdAt) {}
