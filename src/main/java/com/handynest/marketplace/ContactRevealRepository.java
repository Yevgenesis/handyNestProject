package com.handynest.marketplace;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactRevealRepository extends JpaRepository<ContactReveal, Long> {

  @EntityGraph(attributePaths = {"deal", "requestedBy", "subjectUser"})
  Optional<ContactReveal> findByPublicId(String publicId);

  @EntityGraph(attributePaths = {"deal", "requestedBy", "subjectUser"})
  List<ContactReveal> findAllByDealPublicIdOrderByCreatedAtAsc(String dealPublicId);

  Optional<ContactReveal> findTopByDealIdOrderByCreatedAtDesc(Long dealId);

  Optional<ContactReveal> findByDealIdAndRequestedByIdAndSubjectUserIdAndContactType(
      Long dealId, Long requestedById, Long subjectUserId, ContactType contactType);

  boolean existsByDealId(Long dealId);
}
