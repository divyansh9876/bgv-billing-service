package com.bgv.billing.service.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class UsageCommitRequest {
    private UUID reservationId;
}
