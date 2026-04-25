package com.rbs.customer.onboarding.onboarding_service.error;

public class OnboardingNotFoundException extends RuntimeException {
    public OnboardingNotFoundException(String onboardingId) {
        super("Onboarding not found for id: " + onboardingId);
    }
}
