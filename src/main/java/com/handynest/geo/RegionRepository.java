package com.handynest.geo;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionRepository extends JpaRepository<Region, Long> {

    List<Region> findAllByCountryCodeIgnoreCaseOrderByNameRuAsc(String countryCode);

    Optional<Region> findByPublicId(String publicId);
}
