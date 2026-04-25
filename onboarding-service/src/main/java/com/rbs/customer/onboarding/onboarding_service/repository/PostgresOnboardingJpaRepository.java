package com.rbs.customer.onboarding.onboarding_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PostgresOnboardingJpaRepository extends JpaRepository<PostgresOnboardingEntity, String> {
}
