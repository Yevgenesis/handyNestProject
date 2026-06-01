package com.handynest.performer;

import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PerformerProfileRepository
        extends JpaRepository<PerformerProfile, Long>, JpaSpecificationExecutor<PerformerProfile> {

    boolean existsByUserId(Long userId);

    @EntityGraph(attributePaths = {
            "user",
            "baseCountry",
            "baseRegion",
            "baseCity",
            "baseDistrict",
            "categories",
            "categories.category"
    })
    Optional<PerformerProfile> findByUserId(Long userId);

    @EntityGraph(attributePaths = {
            "user",
            "baseCountry",
            "baseRegion",
            "baseCity",
            "baseDistrict",
            "categories",
            "categories.category"
    })
    Optional<PerformerProfile> findByPublicId(String publicId);
}
