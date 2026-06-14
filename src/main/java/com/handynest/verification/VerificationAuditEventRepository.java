package com.handynest.verification;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationAuditEventRepository
    extends JpaRepository<VerificationAuditEvent, Long> {

  long countByVerificationRequestPublicIdAndAction(
      String verificationRequestId, VerificationAuditAction action);
}
