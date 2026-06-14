package com.handynest.marketplace;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.Map;

@Schema(description = "Presigned download contract for a marketplace attachment.")
public record AttachmentDownloadUrlResponse(
    String attachmentId,
    String downloadUrl,
    String downloadMethod,
    Map<String, String> downloadHeaders,
    Instant downloadExpiresAt) {}
