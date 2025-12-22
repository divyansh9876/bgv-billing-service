package com.bgv.billing.service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "usage")
public class Usage extends Auditable {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feature_code", nullable = false)
    private Feature feature;

    @Column(nullable = false)
    private int usedCount = 0;

    @Column(nullable = false)
    private OffsetDateTime periodStart;

    @Column(nullable = false)
    private OffsetDateTime periodEnd;

    @Version
    private Long version;
}
