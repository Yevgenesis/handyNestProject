package com.handynest.marketplace;

import java.time.Instant;
import java.util.Map;

public record AttachmentUploadResponse(
        AttachmentResponse attachment,
        String uploadUrl,
        String uploadMethod,
        Map<String, String> uploadHeaders,
        Instant uploadExpiresAt
) {
}
