package com.bgv.billing.service.repository;

import com.bgv.billing.service.entity.UsageReservation;
import com.bgv.billing.service.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface UsageReservationRepository extends JpaRepository<UsageReservation, UUID> {
    List<UsageReservation> findByStatusAndExpiresAtBefore(ReservationStatus status, OffsetDateTime now);
    List<UsageReservation> findBySubscriptionIdAndFeatureCodeAndStatus(UUID subscriptionId, String featureCode, ReservationStatus status);
}
