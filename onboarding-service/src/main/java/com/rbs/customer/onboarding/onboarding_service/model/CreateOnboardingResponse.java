package com.rbs.customer.onboarding.onboarding_service.model;

public record CreateOnboardingResponse(
        String onboardingId,
        OnboardingStatus status,
        String decisionReason
) {
}
