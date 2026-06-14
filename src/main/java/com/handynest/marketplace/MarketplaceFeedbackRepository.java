package com.handynest.marketplace;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketplaceFeedbackRepository extends JpaRepository<MarketplaceFeedback, Long> {

  boolean existsByDealIdAndSenderId(Long dealId, Long senderId);

  @EntityGraph(
      attributePaths = {
        "task",
        "deal",
        "sender",
        "receiver",
        "deal.performer",
        "deal.performer.user"
      })
  Optional<MarketplaceFeedback> findByPublicId(String publicId);

  @EntityGraph(
      attributePaths = {
        "task",
        "deal",
        "sender",
        "receiver",
        "deal.performer",
        "deal.performer.user"
      })
  List<MarketplaceFeedback> findAllByTaskPublicIdAndHiddenByAdminFalseOrderByCreatedAtDesc(
      String taskPublicId);

  @EntityGraph(
      attributePaths = {
        "task",
        "deal",
        "sender",
        "receiver",
        "deal.performer",
        "deal.performer.user"
      })
  List<MarketplaceFeedback> findAllByReceiverPublicIdAndHiddenByAdminFalseOrderByCreatedAtDesc(
      String receiverPublicId);

  @EntityGraph(
      attributePaths = {
        "task",
        "deal",
        "sender",
        "receiver",
        "deal.performer",
        "deal.performer.user"
      })
  List<MarketplaceFeedback> findAllByDealPerformerPublicIdAndHiddenByAdminFalseOrderByCreatedAtDesc(
      String performerPublicId);
}
