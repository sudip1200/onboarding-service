package com.rbs.customer.onboarding.onboarding_service.model;

import java.time.Instant;

public record CustomerOnboardingRecord(
        String onboardingId,
        String fullName,
        String email,
        String phoneNumber,
        String countryCode,
        String documentId,
        Double monthlyIncome,
        OnboardingStatus status,
        String decisionReason,
        Instant createdAt
) {
}
