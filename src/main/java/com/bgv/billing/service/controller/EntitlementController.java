package com.bgv.billing.service.controller;

import com.bgv.billing.service.dto.EntitlementCheckRequest;
import com.bgv.billing.service.dto.EntitlementCheckResponse;
import com.bgv.billing.service.dto.UsageCommitRequest;
import com.bgv.billing.service.dto.UsageReleaseRequest;
import com.bgv.billing.service.service.EntitlementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/entitlements")
public class EntitlementController {

    @Autowired
    private EntitlementService entitlementService;

    @PostMapping("/check")
    public ResponseEntity<EntitlementCheckResponse> check(@RequestBody EntitlementCheckRequest request) {
        return ResponseEntity.ok(entitlementService.check(request));
    }

    @PostMapping("/usage/commit")
    public ResponseEntity<Void> commit(@RequestBody UsageCommitRequest request) {
        entitlementService.commit(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/usage/release")
    public ResponseEntity<Void> release(@RequestBody UsageReleaseRequest request) {
        entitlementService.release(request);
        return ResponseEntity.ok().build();
    }
}
