package com.bgv.billing.service.repository;

import com.bgv.billing.service.entity.Usage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;
import java.time.OffsetDateTime;

public interface UsageRepository extends JpaRepository<Usage, UUID> {
    Optional<Usage> findBySubscriptionIdAndFeatureCodeAndPeriodStart(UUID subscriptionId, String featureCode, OffsetDateTime periodStart);
}
