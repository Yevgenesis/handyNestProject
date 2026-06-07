package com.handynest.marketplace;

import java.time.Instant;

public record AttachmentResponse(
        String publicId,
        String ownerId,
        String taskId,
        String chatMessageId,
        String disputeCaseId,
        String attachmentType,
        String storageProvider,
        String bucket,
        String storageKey,
        String originalFilename,
        String contentType,
        long sizeBytes,
        String checksum,
        String visibility,
        Instant createdAt
) {
}
