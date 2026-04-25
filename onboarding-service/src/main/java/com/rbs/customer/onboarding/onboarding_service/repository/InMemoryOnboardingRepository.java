package com.rbs.customer.onboarding.onboarding_service.repository;

import com.rbs.customer.onboarding.onboarding_service.model.CustomerOnboardingRecord;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Profile("in-memory")
public class InMemoryOnboardingRepository implements OnboardingRepository {

    private final Map<String, CustomerOnboardingRecord> store = new ConcurrentHashMap<>();

    @Override
    public void save(CustomerOnboardingRecord record) {
        store.put(record.onboardingId(), record);
    }

    @Override
    public Optional<CustomerOnboardingRecord> findById(String onboardingId) {
        return Optional.ofNullable(store.get(onboardingId));
    }
}
