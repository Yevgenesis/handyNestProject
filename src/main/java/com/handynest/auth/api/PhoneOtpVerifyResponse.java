package com.handynest.auth.api;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Phone OTP verification result.")
public record PhoneOtpVerifyResponse(boolean phoneVerified) {}
