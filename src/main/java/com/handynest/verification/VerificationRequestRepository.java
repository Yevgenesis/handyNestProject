package com.handynest.verification;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VerificationRequestRepository extends JpaRepository<VerificationRequest, Long> {

  boolean existsByPerformerProfileIdAndStatus(
      Long performerProfileId, VerificationRequestStatus status);

  @EntityGraph(
      attributePaths = {
        "performerProfile",
        "performerProfile.user",
        "requestedBy",
        "reviewedBy",
        "documents",
        "documents.owner",
        "documents.task",
        "documents.chatMessage",
        "documents.disputeCase"
      })
  Optional<VerificationRequest> findByPublicId(String publicId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select request from VerificationRequest request where request.publicId = :publicId")
  Optional<VerificationRequest> findByPublicIdForUpdate(@Param("publicId") String publicId);

  @EntityGraph(
      attributePaths = {
        "performerProfile",
        "performerProfile.user",
        "requestedBy",
        "reviewedBy",
        "documents",
        "documents.owner",
        "documents.task",
        "documents.chatMessage",
        "documents.disputeCase"
      })
  List<VerificationRequest> findAllByRequestedByIdOrderByCreatedAtDesc(Long requestedById);

  @EntityGraph(
      attributePaths = {
        "performerProfile",
        "performerProfile.user",
        "requestedBy",
        "reviewedBy",
        "documents",
        "documents.owner",
        "documents.task",
        "documents.chatMessage",
        "documents.disputeCase"
      })
  List<VerificationRequest> findAllByStatusOrderByCreatedAtAsc(VerificationRequestStatus status);
}
