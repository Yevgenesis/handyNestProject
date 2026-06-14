package com.handynest.auth.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthLoginRequest(
    @NotBlank @Email @Size(max = 50) String email, @NotBlank String password) {}
