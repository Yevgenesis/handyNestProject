package com.handynest.catalog.category;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategorySynonymRepository extends JpaRepository<CategorySynonym, Long> {

  List<CategorySynonym> findByCategoryIdInAndLocaleIn(
      Collection<Long> categoryIds, Collection<String> locales);

  List<CategorySynonym> findAllByCategoryIdOrderByLocaleAscValueAsc(Long categoryId);

  void deleteAllByCategoryId(Long categoryId);
}
