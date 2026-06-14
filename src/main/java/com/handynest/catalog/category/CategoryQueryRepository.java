package com.handynest.catalog.category;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoryQueryRepository extends JpaRepository<Category, Long> {

  List<Category> findAllByOrderBySortOrderAscIdAsc();

  Optional<Category> findByPublicId(String publicId);

  boolean existsBySlugIgnoreCase(String slug);

  boolean existsBySlugIgnoreCaseAndPublicIdNot(String slug, String publicId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select category from Category category where category.publicId = :publicId")
  Optional<Category> findByPublicIdForUpdate(@Param("publicId") String publicId);
}
