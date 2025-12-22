package com.bgv.billing.service.repository;

import com.bgv.billing.service.entity.PlanFeature;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface PlanFeatureRepository extends JpaRepository<PlanFeature, UUID> {
    Optional<PlanFeature> findByPlanIdAndFeatureCode(UUID planId, String featureCode);
}
