package com.bgv.billing.service.service.impl;

import com.bgv.billing.service.dto.CreatePlanFeatureRequest;
import com.bgv.billing.service.dto.CreatePlanRequest;
import com.bgv.billing.service.dto.PlanDto;
import com.bgv.billing.service.dto.UpdatePriceRequest;
import com.bgv.billing.service.entity.Feature;
import com.bgv.billing.service.entity.Plan;
import com.bgv.billing.service.entity.PlanFeature;
import com.bgv.billing.service.repository.FeatureRepository;
import com.bgv.billing.service.repository.PlanFeatureRepository;
import com.bgv.billing.service.repository.PlanRepository;
import com.bgv.billing.service.service.PlanService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PlanServiceImpl implements PlanService {

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private FeatureRepository featureRepository;

    @Autowired
    private PlanFeatureRepository planFeatureRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PlanDto> getPlans() {
        return planRepository.findAll().stream()
                .filter(Plan::isActive)
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PlanDto createPlan(CreatePlanRequest request) {
        Plan plan = new Plan();
        BeanUtils.copyProperties(request, plan);
        plan = planRepository.save(plan);
        return toDto(plan);
    }

    @Override
    @Transactional
    public PlanDto addFeatureToPlan(UUID planId, CreatePlanFeatureRequest request) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new EntityNotFoundException("Plan not found"));
        Feature feature = featureRepository.findById(request.getFeatureCode())
                .orElseThrow(() -> new EntityNotFoundException("Feature not found"));

        PlanFeature planFeature = new PlanFeature();
        planFeature.setPlan(plan);
        planFeature.setFeature(feature);
        planFeature.setQuota(request.getQuota());
        planFeature.setQuotaType(request.getQuotaType());

        planFeatureRepository.save(planFeature);

        return toDto(plan);
    }

    @Override
    @Transactional
    public void activatePlan(UUID planId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new EntityNotFoundException("Plan not found"));
        plan.setActive(true);
        planRepository.save(plan);
    }

    @Override
    @Transactional
    public void updatePrice(UUID planId, UpdatePriceRequest request) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new EntityNotFoundException("Plan not found"));
        plan.setPrice(request.getPrice());
        plan.setCurrency(request.getCurrency());
        planRepository.save(plan);
    }

    private PlanDto toDto(Plan plan) {
        PlanDto dto = new PlanDto();
        BeanUtils.copyProperties(plan, dto);
        return dto;
    }
}