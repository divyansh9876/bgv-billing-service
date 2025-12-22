package com.bgv.billing.service.service;

import com.bgv.billing.service.dto.CreateSubscriptionRequest;
import com.bgv.billing.service.dto.SubscriptionDto;

import java.util.List;
import java.util.UUID;

public interface SubscriptionService {
    List<SubscriptionDto> getSubscriptions(UUID userId);
    SubscriptionDto createSubscription(UUID planId, CreateSubscriptionRequest request);
}
