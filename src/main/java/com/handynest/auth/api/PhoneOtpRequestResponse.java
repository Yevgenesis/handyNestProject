package com.handynest.auth.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Phone OTP request metadata. Raw OTP is never returned by API.")
public record PhoneOtpRequestResponse(
    Instant expiresAt, long retryAfterSeconds, String maskedPhone) {}
