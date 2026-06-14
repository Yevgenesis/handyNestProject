package com.handynest.risk;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRiskAdjustmentRepository
    extends JpaRepository<CustomerRiskAdjustment, Long> {

  Optional<CustomerRiskAdjustment> findBySourceTypeAndSourcePublicId(
      CustomerRiskSourceType sourceType, String sourcePublicId);
}
