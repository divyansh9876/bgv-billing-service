package com.bgv.billing.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntitlementCheckResponse {
    private boolean allowed;
    private UUID reservationId;
    private String reason;
}
