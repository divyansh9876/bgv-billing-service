package com.bgv.billing.service.service.impl;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bgv.billing.service.dto.EntitlementCheckRequest;
import com.bgv.billing.service.dto.EntitlementCheckResponse;
import com.bgv.billing.service.dto.UsageCommitRequest;
import com.bgv.billing.service.dto.UsageReleaseRequest;
import com.bgv.billing.service.entity.PlanFeature;
import com.bgv.billing.service.entity.Subscription;
import com.bgv.billing.service.entity.Usage;
import com.bgv.billing.service.entity.UsageReservation;
import com.bgv.billing.service.enums.QuotaType;
import com.bgv.billing.service.enums.ReservationStatus;
import com.bgv.billing.service.enums.SubscriptionStatus;
import com.bgv.billing.service.repository.FeatureRepository;
import com.bgv.billing.service.repository.PlanFeatureRepository;
import com.bgv.billing.service.repository.SubscriptionRepository;
import com.bgv.billing.service.repository.UsageRepository;
import com.bgv.billing.service.repository.UsageReservationRepository;
import com.bgv.billing.service.service.EntitlementService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.LockModeType;

@Service
public class EntitlementServiceImpl implements EntitlementService {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private PlanFeatureRepository planFeatureRepository;

    @Autowired
    private UsageRepository usageRepository;

    @Autowired
    private UsageReservationRepository usageReservationRepository;
    
    @Autowired
    private FeatureRepository featureRepository;


    @Override
    @Transactional
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    public EntitlementCheckResponse check(EntitlementCheckRequest request) {
        // 1. Find active subscription
        List<Subscription> subscriptions = subscriptionRepository.findByUserIdAndStatus(request.getUserId(), SubscriptionStatus.ACTIVE);
        if (subscriptions.isEmpty() && request.getOrgId() != null) {
            subscriptions = subscriptionRepository.findByOrgIdAndStatus(request.getOrgId(), SubscriptionStatus.ACTIVE);
        }

        if (subscriptions.isEmpty()) {
            return new EntitlementCheckResponse(false, null, "No active subscription found.");
        }
        // For simplicity, we use the first active subscription found.
        // A more complex logic could be implemented to select a specific subscription.
        Subscription subscription = subscriptions.get(0);

        // 2. Find the corresponding plan feature
        PlanFeature planFeature = planFeatureRepository.findByPlanIdAndFeatureCode(subscription.getPlan().getId(), request.getFeatureCode())
                .orElseThrow(() -> new EntityNotFoundException("Feature not available in the current plan."));

        // 3. Check for UNLIMITED quota
        if (planFeature.getQuotaType() == QuotaType.UNLIMITED) {
            return new EntitlementCheckResponse(true, null, "UNLIMITED quota");
        }

        // 4. Handle LIMITED quota
        // Determine current billing period
        OffsetDateTime periodStart = subscription.getStartDate();
        OffsetDateTime periodEnd = subscription.getEndDate();
        if (subscription.getPlan().getBillingPeriod() == com.bgv.billing.service.enums.BillingPeriod.MONTHLY) {
            periodStart = OffsetDateTime.now().withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
            periodEnd = periodStart.plusMonths(1).minusNanos(1);
        } else if (subscription.getPlan().getBillingPeriod() == com.bgv.billing.service.enums.BillingPeriod.YEARLY) {
            periodStart = OffsetDateTime.now().withDayOfYear(1).truncatedTo(ChronoUnit.DAYS);
            periodEnd = periodStart.plusYears(1).minusNanos(1);
        }


        // 5. Get current usage and reserved counts
        Usage usage = usageRepository.findBySubscriptionIdAndFeatureCodeAndPeriodStart(subscription.getId(), request.getFeatureCode(), periodStart)
                .orElse(new Usage());
        
        List<UsageReservation> reservations = usageReservationRepository.findBySubscriptionIdAndFeatureCodeAndStatus(subscription.getId(), request.getFeatureCode(), ReservationStatus.RESERVED);
        int reservedCount = reservations.stream().mapToInt(UsageReservation::getReservedCount).sum();

        // 6. Check if quota is sufficient
        if (usage.getUsedCount() + reservedCount + request.getCount() > planFeature.getQuota()) {
            return new EntitlementCheckResponse(false, null, "Quota exceeded.");
        }

        // 7. Create a reservation
        UsageReservation reservation = new UsageReservation();
        reservation.setSubscription(subscription);
        reservation.setFeature(featureRepository.findById(request.getFeatureCode()).get());
        reservation.setReservedCount(request.getCount());
        reservation.setStatus(ReservationStatus.RESERVED);
        reservation.setExpiresAt(OffsetDateTime.now().plusMinutes(5)); // Reservation expires in 5 minutes
        reservation = usageReservationRepository.save(reservation);

        return new EntitlementCheckResponse(true, reservation.getId(), "Reservation successful.");
    }

    @Override
    @Transactional
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    public void commit(UsageCommitRequest request) {
        UsageReservation reservation = usageReservationRepository.findById(request.getReservationId())
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found."));

        if (reservation.getStatus() != ReservationStatus.RESERVED) {
            // Or throw an exception
            return;
        }
        
        if (reservation.getExpiresAt().isBefore(OffsetDateTime.now())) {
            reservation.setStatus(ReservationStatus.RELEASED);
            usageReservationRepository.save(reservation);
            // Or throw an exception
            return;
        }

        reservation.setStatus(ReservationStatus.COMMITTED);
        usageReservationRepository.save(reservation);

        // Update usage
        Subscription subscription = reservation.getSubscription();
        OffsetDateTime periodStart = subscription.getStartDate();
         if (subscription.getPlan().getBillingPeriod() == com.bgv.billing.service.enums.BillingPeriod.MONTHLY) {
            periodStart = OffsetDateTime.now().withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
        } else if (subscription.getPlan().getBillingPeriod() == com.bgv.billing.service.enums.BillingPeriod.YEARLY) {
            periodStart = OffsetDateTime.now().withDayOfYear(1).truncatedTo(ChronoUnit.DAYS);
        }

        final OffsetDateTime effectivePeriodStart = periodStart;
        Usage usage = usageRepository.findBySubscriptionIdAndFeatureCodeAndPeriodStart(subscription.getId(), reservation.getFeature().getCode(), effectivePeriodStart)
                .orElseGet(() -> {
                    Usage newUsage = new Usage();
                    newUsage.setSubscription(subscription);
                    newUsage.setFeature(reservation.getFeature());
                    newUsage.setPeriodStart(effectivePeriodStart);
                    newUsage.setPeriodEnd(subscription.getEndDate());
                    return newUsage;
                });

        usage.setUsedCount(usage.getUsedCount() + reservation.getReservedCount());
        usageRepository.save(usage);
    }

    @Override
    @Transactional
    public void release(UsageReleaseRequest request) {
        UsageReservation reservation = usageReservationRepository.findById(request.getReservationId())
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found."));

        if (reservation.getStatus() == ReservationStatus.RESERVED) {
            reservation.setStatus(ReservationStatus.RELEASED);
            usageReservationRepository.save(reservation);
        }
    }

    @Scheduled(fixedRate = 60000) // Run every minute
    @Transactional
    public void cleanupExpiredReservations() {
        List<UsageReservation> expiredReservations = usageReservationRepository.findByStatusAndExpiresAtBefore(ReservationStatus.RESERVED, OffsetDateTime.now());
        for (UsageReservation reservation : expiredReservations) {
            reservation.setStatus(ReservationStatus.RELEASED);
        }
        usageReservationRepository.saveAll(expiredReservations);
    }
}