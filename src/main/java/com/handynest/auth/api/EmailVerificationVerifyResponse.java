package com.handynest.auth.api;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Email verification result.")
public record EmailVerificationVerifyResponse(boolean emailVerified) {}
