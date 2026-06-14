package com.handynest.verification;

import com.handynest.marketplace.AttachmentResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

@Schema(description = "Performer verification request and review result.")
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
    Instant updatedAt) {
  public VerificationRequestResponse {
    documents = documents == null ? List.of() : List.copyOf(documents);
  }
}
