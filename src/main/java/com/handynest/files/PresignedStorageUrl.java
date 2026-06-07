package com.handynest.files;

import java.time.Instant;
import java.util.Map;

public record PresignedStorageUrl(
        String url,
        String method,
        Map<String, String> headers,
        Instant expiresAt
) {
}
