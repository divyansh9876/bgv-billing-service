package com.bgv.billing.service.service;

import com.bgv.billing.service.dto.EntitlementCheckRequest;
import com.bgv.billing.service.dto.EntitlementCheckResponse;
import com.bgv.billing.service.dto.UsageCommitRequest;
import com.bgv.billing.service.dto.UsageReleaseRequest;

public interface EntitlementService {
    EntitlementCheckResponse check(EntitlementCheckRequest request);
    void commit(UsageCommitRequest request);
    void release(UsageReleaseRequest request);
}
