package com.handynest.verification;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Metadata for a private verification document upload.")
public record VerificationDocumentUploadRequest(
    @Schema(example = "identity-front.pdf") @NotBlank @Size(max = 255) String originalFilename,
    @Schema(example = "application/pdf") @NotBlank @Size(max = 120) String contentType,
    @Schema(example = "524288", maximum = "20971520") @Min(1) @Max(20_971_520) long sizeBytes,
    @Schema(example = "sha256:...") @Size(max = 128) String checksum,
    @Schema(example = "IDENTITY_DOCUMENT") @NotNull VerificationDocumentType documentType) {}
