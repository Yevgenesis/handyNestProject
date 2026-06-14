package com.handynest.auth.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Email verification token request.")
public record EmailVerificationVerifyRequest(@NotBlank @Size(max = 256) String token) {}
