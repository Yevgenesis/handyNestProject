package com.handynest.identity;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserConsentRepository extends JpaRepository<UserConsent, Long> {

  boolean existsByUserIdAndConsentTypeAndDocumentVersion(
      Long userId, ConsentType type, String version);

  List<UserConsent> findAllByUserIdOrderByAcceptedAtDesc(Long userId);
}
