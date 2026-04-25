package com.rbs.customer.onboarding.onboarding_service.repository;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "customer_onboarding")
@Getter
@Setter
public class PostgresOnboardingEntity {

    @Id
    @Column(name = "onboarding_id", nullable = false, length = 64)
    private String onboardingId;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "email", nullable = false, length = 120)
    private String email;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "country_code", nullable = false, length = 8)
    private String countryCode;

    @Column(name = "document_id", nullable = false, length = 64)
    private String documentId;

    @Column(name = "monthly_income", nullable = false)
    private Double monthlyIncome;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "decision_reason", nullable = false, length = 255)
    private String decisionReason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
