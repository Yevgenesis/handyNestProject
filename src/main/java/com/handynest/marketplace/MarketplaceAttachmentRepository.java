package com.handynest.marketplace;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;

public interface MarketplaceAttachmentRepository extends JpaRepository<MarketplaceAttachment, Long> {

    @EntityGraph(attributePaths = {
            "owner",
            "task",
            "chatMessage",
            "disputeCase",
            "disputeCase.deal",
            "disputeCase.deal.customer",
            "disputeCase.deal.performer",
            "disputeCase.deal.performer.user"
    })
    Optional<MarketplaceAttachment> findByPublicIdAndDeletedAtIsNull(String publicId);

    @EntityGraph(attributePaths = {"owner", "task", "chatMessage"})
    List<MarketplaceAttachment> findAllByChatMessageChatPublicIdAndDeletedAtIsNullOrderByCreatedAtDesc(String chatId);

    @EntityGraph(attributePaths = {"owner", "task", "disputeCase"})
    List<MarketplaceAttachment> findAllByDisputeCasePublicIdAndDeletedAtIsNullOrderByCreatedAtDesc(String disputeId);

    @EntityGraph(attributePaths = {"owner", "task", "chatMessage", "disputeCase"})
    List<MarketplaceAttachment> findAllByOwnerIdAndAttachmentTypeAndDeletedAtIsNullOrderByCreatedAtDesc(
            Long ownerId,
            AttachmentType attachmentType
    );

    @Query("""
            select attachment
            from MarketplaceAttachment attachment
            join attachment.verificationRequest request
            where attachment.deletedAt is null
              and attachment.attachmentType = com.handynest.marketplace.AttachmentType.VERIFICATION_DOCUMENT
              and request.status = com.handynest.verification.VerificationRequestStatus.REJECTED
              and request.reviewedAt < :reviewedBefore
            order by attachment.id asc
            """)
    List<MarketplaceAttachment> findRejectedVerificationDocumentsForRetention(
            @Param("reviewedBefore") Instant reviewedBefore,
            Pageable pageable
    );
}
