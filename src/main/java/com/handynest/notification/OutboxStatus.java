package com.handynest.notification;

public enum OutboxStatus {
    PENDING,
    PROCESSING,
    PROCESSED,
    FAILED,
    DEAD_LETTER
}
