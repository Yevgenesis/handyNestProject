package com.handynest.geo;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DistrictRepository extends JpaRepository<District, Long> {

  List<District> findAllByCityPublicIdAndSupportedTrueOrderByNameRuAsc(String cityPublicId);

  Optional<District> findByPublicId(String publicId);
}
