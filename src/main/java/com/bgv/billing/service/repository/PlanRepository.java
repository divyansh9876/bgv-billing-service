package com.bgv.billing.service.repository;

import com.bgv.billing.service.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PlanRepository extends JpaRepository<Plan, UUID> {
}
