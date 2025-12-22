package com.bgv.billing.service.service.impl;

import com.bgv.billing.service.dto.CreateSubscriptionRequest;
import com.bgv.billing.service.dto.PlanDto;
import com.bgv.billing.service.dto.SubscriptionDto;
import com.bgv.billing.service.entity.Plan;
import com.bgv.billing.service.entity.Subscription;
import com.bgv.billing.service.enums.SubscriptionStatus;
import com.bgv.billing.service.repository.PlanRepository;
import com.bgv.billing.service.repository.SubscriptionRepository;
import com.bgv.billing.service.service.SubscriptionService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private PlanRepository planRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionDto> getSubscriptions(UUID userId) {
        return subscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SubscriptionDto createSubscription(UUID planId, CreateSubscriptionRequest request) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new EntityNotFoundException("Plan not found"));

        Subscription subscription = new Subscription();
        subscription.setPlan(plan);
        subscription.setUserId(request.getUserId());
        subscription.setOrgId(request.getOrgId());
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartDate(OffsetDateTime.now());

        OffsetDateTime endDate = null;
        switch (plan.getBillingPeriod()) {
            case MONTHLY:
                endDate = OffsetDateTime.now().plusMonths(1);
                break;
            case YEARLY:
                endDate = OffsetDateTime.now().plusYears(1);
                break;
            case ONE_TIME:
                // No end date for one-time purchases
                break;
        }
        subscription.setEndDate(endDate);

        subscription = subscriptionRepository.save(subscription);
        return toDto(subscription);
    }

    private SubscriptionDto toDto(Subscription subscription) {
        SubscriptionDto dto = new SubscriptionDto();
        BeanUtils.copyProperties(subscription, dto);
        
        PlanDto planDto = new PlanDto();
        BeanUtils.copyProperties(subscription.getPlan(), planDto);
        dto.setPlan(planDto);
        
        return dto;
    }
}