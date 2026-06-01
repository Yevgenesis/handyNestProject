package com.handynest.identity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BusinessProfileRequest(
        @NotBlank
        @Size(max = 255)
        String companyName,

        @Size(max = 32)
        String bin,

        @Size(max = 500)
        String legalAddress,

        @Size(max = 255)
        String billingEmail,

        @Size(max = 255)
        String contactPersonName,

        @Size(max = 32)
        String contactPersonPhone
) {
}
