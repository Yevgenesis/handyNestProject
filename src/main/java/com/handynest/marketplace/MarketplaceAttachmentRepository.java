package com.handynest.marketplace;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MarketplaceAttachmentRepository
    extends JpaRepository<MarketplaceAttachment, Long> {

  @EntityGraph(
      attributePaths = {
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

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      """
      select attachment
      from MarketplaceAttachment attachment
      left join fetch attachment.owner
      left join fetch attachment.task
      left join fetch attachment.chatMessage message
      left join fetch message.chat
      where attachment.publicId = :publicId
        and attachment.deletedAt is null
      """)
  Optional<MarketplaceAttachment> findByPublicIdForUpdate(@Param("publicId") String publicId);

  @EntityGraph(attributePaths = {"owner", "task", "chatMessage"})
  List<MarketplaceAttachment>
      findAllByChatMessageChatPublicIdAndDeletedAtIsNullOrderByCreatedAtDesc(String chatId);

  @EntityGraph(attributePaths = {"owner", "task", "disputeCase"})
  List<MarketplaceAttachment> findAllByDisputeCasePublicIdAndDeletedAtIsNullOrderByCreatedAtDesc(
      String disputeId);

  @EntityGraph(attributePaths = {"owner", "task"})
  List<MarketplaceAttachment>
      findAllByTaskPublicIdAndAttachmentTypeAndDeletedAtIsNullOrderByCreatedAtAsc(
          String taskId, AttachmentType attachmentType);

  @EntityGraph(attributePaths = {"owner", "task", "chatMessage", "disputeCase"})
  List<MarketplaceAttachment>
      findAllByOwnerIdAndAttachmentTypeAndDeletedAtIsNullOrderByCreatedAtDesc(
          Long ownerId, AttachmentType attachmentType);

  @Query(
      """
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
      @Param("reviewedBefore") Instant reviewedBefore, Pageable pageable);

  List<MarketplaceAttachment> findAllByDeletedAtIsNotNullAndStorageDeletedAtIsNullOrderByIdAsc(
      Pageable pageable);
}
