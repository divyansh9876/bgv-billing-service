package com.bgv.billing.service.controller;

import com.bgv.billing.service.dto.CreateSubscriptionRequest;
import com.bgv.billing.service.dto.PlanDto;
import com.bgv.billing.service.dto.SubscriptionDto;
import com.bgv.billing.service.service.PlanService;
import com.bgv.billing.service.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
public class SubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private PlanService planService;

    @GetMapping("/plans")
    public ResponseEntity<List<PlanDto>> getPlans() {
        return ResponseEntity.ok(planService.getPlans());
    }

    @GetMapping("/subscriptions/me")
    public ResponseEntity<List<SubscriptionDto>> getMySubscriptions() {
        // In a real application, the userId would be retrieved from the JWT token in the security context
        UUID userId = UUID.randomUUID(); // Placeholder
        return ResponseEntity.ok(subscriptionService.getSubscriptions(userId));
    }

    @PostMapping("/subscriptions/{planId}")
    public ResponseEntity<SubscriptionDto> createSubscription(@PathVariable UUID planId, @RequestBody CreateSubscriptionRequest request) {
        return ResponseEntity.ok(subscriptionService.createSubscription(planId, request));
    }
}
