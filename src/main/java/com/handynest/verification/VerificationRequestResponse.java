package com.handynest.verification;

import com.handynest.marketplace.AttachmentResponse;
import java.time.Instant;
import java.util.List;

public record VerificationRequestResponse(
        String publicId,
        String performerId,
        String requestedByUserId,
        String requestedLevel,
        String status,
        String comment,
        String reviewedByUserId,
        Instant reviewedAt,
        String rejectionReason,
        List<AttachmentResponse> documents,
        Instant createdAt,
        Instant updatedAt
) {
    public VerificationRequestResponse {
        documents = documents == null ? List.of() : List.copyOf(documents);
    }
}
