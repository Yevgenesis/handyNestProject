package com.handynest.marketplace;

import java.time.Instant;
import java.util.Map;

public record AttachmentDownloadUrlResponse(
        String attachmentId,
        String downloadUrl,
        String downloadMethod,
        Map<String, String> downloadHeaders,
        Instant downloadExpiresAt
) {
}
