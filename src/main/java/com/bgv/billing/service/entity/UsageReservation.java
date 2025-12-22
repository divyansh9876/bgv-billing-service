package com.bgv.billing.service.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.bgv.billing.service.enums.ReservationStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "usage_reservation")
public class UsageReservation extends Auditable {

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
    private int reservedCount;
    
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Enumerated(EnumType.STRING)
    @Column(
        name = "status",
        nullable = false,
        columnDefinition = "reservation_status"
    )
    
    private ReservationStatus status;

    @Column(nullable = false)
    private OffsetDateTime expiresAt;

    @Version
    private Long version;
}
