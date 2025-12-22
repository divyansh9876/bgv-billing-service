package com.bgv.billing.service.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class EntitlementCheckRequest {
    private UUID userId;
    private UUID orgId;
    private String featureCode;
    private int count = 1;
}
