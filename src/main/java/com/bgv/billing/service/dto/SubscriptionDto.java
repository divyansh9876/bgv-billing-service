package com.bgv.billing.service.dto;

import com.bgv.billing.service.enums.SubscriptionStatus;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class SubscriptionDto {
    private UUID id;
    private UUID userId;
    private UUID orgId;
    private PlanDto plan;
    private SubscriptionStatus status;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
}
