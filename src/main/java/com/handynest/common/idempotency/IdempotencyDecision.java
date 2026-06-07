package com.handynest.common.idempotency;

public record IdempotencyDecision(IdempotencyKey key, boolean replay) {
}
