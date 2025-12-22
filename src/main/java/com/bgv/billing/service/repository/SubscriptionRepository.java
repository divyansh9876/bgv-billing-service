package com.bgv.billing.service.repository;

import com.bgv.billing.service.entity.Subscription;
import com.bgv.billing.service.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    List<Subscription> findByUserIdAndStatus(UUID userId, SubscriptionStatus status);
    List<Subscription> findByOrgIdAndStatus(UUID orgId, SubscriptionStatus status);
}
