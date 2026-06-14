package com.handynest.auth.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Email verification request metadata. Raw token is never returned by API.")
public record EmailVerificationRequestResponse(
    Instant expiresAt, long retryAfterSeconds, String maskedEmail) {}
