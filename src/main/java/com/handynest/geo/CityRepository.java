package com.handynest.geo;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CityRepository extends JpaRepository<City, Long> {

    List<City> findAllByRegionPublicIdOrderBySortOrderAscIdAsc(String regionPublicId);

    Optional<City> findByPublicId(String publicId);
}
