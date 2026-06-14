package com.handynest.marketplace;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Public task image metadata without internal storage coordinates.")
public record TaskAttachmentResponse(
    String publicId,
    String taskId,
    String originalFilename,
    String contentType,
    long sizeBytes,
    Instant createdAt) {}
