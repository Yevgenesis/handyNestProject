package com.handynest.marketplace;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to ask the performer for revision after work submission.")
public record RevisionRequest(@NotBlank @Size(max = 2000) String reason) {}
