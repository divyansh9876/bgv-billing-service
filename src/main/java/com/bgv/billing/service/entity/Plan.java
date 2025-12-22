package com.bgv.billing.service.entity;

import java.math.BigDecimal;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.bgv.billing.service.enums.BillingPeriod;
import com.bgv.billing.service.enums.PlanType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "plan")
public class Plan extends Auditable {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Enumerated(EnumType.STRING)
    @Column(
        name = "type",
        nullable = false,
        columnDefinition = "plan_type"
    )
    private PlanType type;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private String currency;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Enumerated(EnumType.STRING)
    @Column(
        name = "billingPeriod",
        nullable = false,
        columnDefinition = "billing_period"
    )
    private BillingPeriod billingPeriod;

    @Column(nullable = false)
    private boolean isActive = true;
}