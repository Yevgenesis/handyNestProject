package com.handynest.auth.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Phone OTP verification request.")
public record PhoneOtpVerifyRequest(@NotBlank @Pattern(regexp = "^\\d{6}$") String code) {}
