package com.handynest.verification;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationRequestRepository extends JpaRepository<VerificationRequest, Long> {

    boolean existsByPerformerProfileIdAndStatus(Long performerProfileId, VerificationRequestStatus status);

    @EntityGraph(attributePaths = {
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

    @EntityGraph(attributePaths = {
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

    @EntityGraph(attributePaths = {
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
