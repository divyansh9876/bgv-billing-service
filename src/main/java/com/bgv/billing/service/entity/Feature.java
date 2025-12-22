package com.bgv.billing.service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "feature")
public class Feature {

    @Id
    private String code;

    private String description;
}
