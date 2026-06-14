package com.handynest.marketplace;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Attachment metadata request used before receiving a presigned upload URL.")
public record AttachmentMetadataRequest(
    @NotBlank @Size(max = 255) String originalFilename,
    @NotBlank @Size(max = 120) String contentType,
    @Min(1) @Max(26_214_400) long sizeBytes,
    @Size(max = 128) String checksum,
    @Size(max = 4000) String text) {}
