package com.bgv.billing.service.repository;

import com.bgv.billing.service.entity.Feature;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeatureRepository extends JpaRepository<Feature, String> {
}
