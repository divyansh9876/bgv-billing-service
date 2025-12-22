package com.bgv.billing.service.controller;

import com.bgv.billing.service.dto.CreatePlanFeatureRequest;
import com.bgv.billing.service.dto.CreatePlanRequest;
import com.bgv.billing.service.dto.PlanDto;
import com.bgv.billing.service.dto.UpdatePriceRequest;
import com.bgv.billing.service.service.PlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/plans")
public class AdminController {

    @Autowired
    private PlanService planService;

    @PostMapping
    public ResponseEntity<PlanDto> createPlan(@RequestBody CreatePlanRequest request) {
        return ResponseEntity.ok(planService.createPlan(request));
    }

    @PostMapping("/{planId}/features")
    public ResponseEntity<PlanDto> addFeatureToPlan(@PathVariable UUID planId, @RequestBody CreatePlanFeatureRequest request) {
        return ResponseEntity.ok(planService.addFeatureToPlan(planId, request));
    }

    @PatchMapping("/{planId}/activate")
    public ResponseEntity<Void> activatePlan(@PathVariable UUID planId) {
        planService.activatePlan(planId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{planId}/price")
    public ResponseEntity<Void> updatePrice(@PathVariable UUID planId, @RequestBody UpdatePriceRequest request) {
        planService.updatePrice(planId, request);
        return ResponseEntity.ok().build();
    }
}
