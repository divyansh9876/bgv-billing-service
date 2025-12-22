package com.bgv.billing.service.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class CreateSubscriptionRequest {
    private UUID userId;
    private UUID orgId;
}
