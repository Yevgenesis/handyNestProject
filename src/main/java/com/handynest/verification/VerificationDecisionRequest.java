package com.handynest.verification;

import jakarta.validation.constraints.Size;

public record VerificationDecisionRequest(@Size(max = 1000) String reason) {}
