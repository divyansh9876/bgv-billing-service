package com.bgv.billing.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing(dateTimeProviderRef = "dateTimeProvider")
@EnableScheduling
public class BgvBillingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BgvBillingServiceApplication.class, args);
    }

}
