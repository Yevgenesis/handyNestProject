package com.handynest.retention;

public record DataRetentionRunResult(
    long anonymizedDeletedUsers, long expiredVerificationDocuments) {}
