package com.handynest.marketplace;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Safe marketplace attachment metadata without internal storage location.")
public record AttachmentResponse(
    String publicId,
    String ownerId,
    String taskId,
    String chatMessageId,
    String disputeCaseId,
    String attachmentType,
    String originalFilename,
    String contentType,
    long sizeBytes,
    String verificationDocumentType,
    Instant createdAt) {}
