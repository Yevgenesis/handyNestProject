package com.handynest.catalog.category;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryTranslationRepository extends JpaRepository<CategoryTranslation, Long> {

    List<CategoryTranslation> findByCategoryIdInAndLocaleIn(
            Collection<Long> categoryIds,
            Collection<String> locales
    );
}
