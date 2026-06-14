package com.handynest.marketplace;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Optional performer message when submitting work for acceptance.")
public record WorkSubmissionRequest(@Size(max = 2000) String message) {}
