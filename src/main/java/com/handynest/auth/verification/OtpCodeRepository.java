package com.handynest.auth.verification;

import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {

  Optional<OtpCode>
      findFirstByUserIdAndPhoneNumberAndPurposeAndConsumedAtIsNullOrderByCreatedAtDesc(
          Long userId, String phoneNumber, OtpPurpose purpose);

  @Modifying
  @Query(
      """
            update OtpCode otp
               set otp.consumedAt = :consumedAt
             where otp.user.id = :userId
               and otp.phoneNumber = :phoneNumber
               and otp.purpose = :purpose
               and otp.consumedAt is null
            """)
  int consumeActiveCodes(
      @Param("userId") Long userId,
      @Param("phoneNumber") String phoneNumber,
      @Param("purpose") OtpPurpose purpose,
      @Param("consumedAt") Instant consumedAt);
}
