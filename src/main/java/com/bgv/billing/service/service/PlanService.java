package com.bgv.billing.service.service;

import com.bgv.billing.service.dto.CreatePlanFeatureRequest;
import com.bgv.billing.service.dto.CreatePlanRequest;
import com.bgv.billing.service.dto.PlanDto;
import com.bgv.billing.service.dto.UpdatePriceRequest;

import java.util.List;
import java.util.UUID;

public interface PlanService {
    List<PlanDto> getPlans();
    PlanDto createPlan(CreatePlanRequest request);
    PlanDto addFeatureToPlan(UUID planId, CreatePlanFeatureRequest request);
    void activatePlan(UUID planId);
    void updatePrice(UUID planId, UpdatePriceRequest request);
}
