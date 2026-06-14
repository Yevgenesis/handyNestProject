package com.handynest.catalog.category;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryAuditEventRepository extends JpaRepository<CategoryAuditEvent, Long> {

  @EntityGraph(attributePaths = "actor")
  List<CategoryAuditEvent> findAllByCategoryPublicIdOrderByCreatedAtDesc(String categoryPublicId);
}
