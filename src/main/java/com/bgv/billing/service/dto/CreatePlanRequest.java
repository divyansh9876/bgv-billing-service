package com.bgv.billing.service.dto;

import com.bgv.billing.service.enums.BillingPeriod;
import com.bgv.billing.service.enums.PlanType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePlanRequest {
    private String code;
    private String name;
    private PlanType type;
    private BigDecimal price;
    private String currency;
    private BillingPeriod billingPeriod;
}
