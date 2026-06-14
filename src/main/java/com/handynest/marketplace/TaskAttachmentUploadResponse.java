package com.handynest.marketplace;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.Map;

@Schema(description = "Presigned upload contract for a public task image.")
public record TaskAttachmentUploadResponse(
    TaskAttachmentResponse attachment,
    String uploadUrl,
    String uploadMethod,
    Map<String, String> uploadHeaders,
    Instant uploadExpiresAt) {}
