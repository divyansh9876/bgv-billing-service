package com.bgv.billing.service.dto;

import com.bgv.billing.service.enums.QuotaType;
import lombok.Data;

@Data
public class CreatePlanFeatureRequest {
    private String featureCode;
    private Integer quota;
    private QuotaType quotaType;
}
