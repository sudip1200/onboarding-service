package com.rbs.customer.onboarding.onboarding_service.repository;

import com.rbs.customer.onboarding.onboarding_service.model.CustomerOnboardingRecord;

import java.util.Optional;

public interface OnboardingRepository {
    void save(CustomerOnboardingRecord record);

    Optional<CustomerOnboardingRecord> findById(String onboardingId);
}
