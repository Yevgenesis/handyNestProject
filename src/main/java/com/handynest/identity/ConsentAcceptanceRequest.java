package com.handynest.identity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ConsentAcceptanceRequest(
    @NotNull ConsentType type, @NotBlank @Size(max = 32) String documentVersion) {}
