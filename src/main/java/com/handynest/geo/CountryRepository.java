package com.handynest.geo;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryRepository extends JpaRepository<Country, Long> {

    List<Country> findAllByOrderByCodeAsc();

    Optional<Country> findByCodeIgnoreCase(String code);
}
