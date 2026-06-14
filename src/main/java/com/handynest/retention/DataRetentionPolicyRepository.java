package com.handynest.retention;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataRetentionPolicyRepository extends JpaRepository<DataRetentionPolicy, Long> {

  Optional<DataRetentionPolicy> findFirstByActiveTrueOrderByIdAsc();
}
