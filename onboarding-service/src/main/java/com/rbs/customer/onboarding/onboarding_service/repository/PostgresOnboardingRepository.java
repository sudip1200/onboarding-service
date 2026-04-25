package com.rbs.customer.onboarding.onboarding_service.repository;

import com.rbs.customer.onboarding.onboarding_service.model.CustomerOnboardingRecord;
import com.rbs.customer.onboarding.onboarding_service.model.OnboardingStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Profile("postgres")
@RequiredArgsConstructor
public class PostgresOnboardingRepository implements OnboardingRepository {

    private final PostgresOnboardingJpaRepository jpaRepository;

    @Override
    public void save(CustomerOnboardingRecord record) {
        jpaRepository.save(toEntity(record));
    }

    @Override
    public Optional<CustomerOnboardingRecord> findById(String onboardingId) {
        return jpaRepository.findById(onboardingId).map(this::toRecord);
    }

    private PostgresOnboardingEntity toEntity(CustomerOnboardingRecord record) {
        PostgresOnboardingEntity entity = new PostgresOnboardingEntity();
        entity.setOnboardingId(record.onboardingId());
        entity.setFullName(record.fullName());
        entity.setEmail(record.email());
        entity.setPhoneNumber(record.phoneNumber());
        entity.setCountryCode(record.countryCode());
        entity.setDocumentId(record.documentId());
        entity.setMonthlyIncome(record.monthlyIncome());
        entity.setStatus(record.status().name());
        entity.setDecisionReason(record.decisionReason());
        entity.setCreatedAt(record.createdAt());
        return entity;
    }

    private CustomerOnboardingRecord toRecord(PostgresOnboardingEntity entity) {
        return new CustomerOnboardingRecord(
                entity.getOnboardingId(),
                entity.getFullName(),
                entity.getEmail(),
                entity.getPhoneNumber(),
                entity.getCountryCode(),
                entity.getDocumentId(),
                entity.getMonthlyIncome(),
                OnboardingStatus.valueOf(entity.getStatus()),
                entity.getDecisionReason(),
                entity.getCreatedAt()
        );
    }
}
