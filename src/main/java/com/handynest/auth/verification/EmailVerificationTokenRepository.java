package com.handynest.auth.verification;

import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailVerificationTokenRepository
    extends JpaRepository<EmailVerificationToken, Long> {

  Optional<EmailVerificationToken> findByTokenHash(String tokenHash);

  Optional<EmailVerificationToken> findFirstByUserIdAndEmailAndConsumedAtIsNullOrderByCreatedAtDesc(
      Long userId, String email);

  @Modifying
  @Query(
      """
            update EmailVerificationToken token
               set token.consumedAt = :consumedAt
             where token.user.id = :userId
               and token.email = :email
               and token.consumedAt is null
            """)
  int consumeActiveTokens(
      @Param("userId") Long userId,
      @Param("email") String email,
      @Param("consumedAt") Instant consumedAt);
}
