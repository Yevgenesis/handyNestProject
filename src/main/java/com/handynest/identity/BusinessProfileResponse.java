package com.handynest.identity;

public record BusinessProfileResponse(
        String companyName,
        String bin,
        String legalAddress,
        String billingEmail,
        String contactPersonName,
        String contactPersonPhone,
        String verificationStatus,
        String rejectionReason
) {
}
