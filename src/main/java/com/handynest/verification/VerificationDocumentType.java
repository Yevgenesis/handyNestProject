package com.handynest.verification;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Purpose of a private verification document.")
public enum VerificationDocumentType {
  IDENTITY_DOCUMENT,
  SELFIE,
  BUSINESS_DOCUMENT,
  LICENSE,
  OTHER
}
