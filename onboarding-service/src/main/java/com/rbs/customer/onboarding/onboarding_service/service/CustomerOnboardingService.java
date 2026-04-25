package com.rbs.customer.onboarding.onboarding_service.service;

import com.rbs.customer.onboarding.onboarding_service.error.OnboardingNotFoundException;
import com.rbs.customer.onboarding.onboarding_service.model.CreateOnboardingRequest;
import com.rbs.customer.onboarding.onboarding_service.model.CreateOnboardingResponse;
import com.rbs.customer.onboarding.onboarding_service.model.CustomerOnboardingRecord;
import com.rbs.customer.onboarding.onboarding_service.model.OnboardingDetailsResponse;
import com.rbs.customer.onboarding.onboarding_service.model.OnboardingStatus;
import com.rbs.customer.onboarding.onboarding_service.repository.OnboardingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomerOnboardingService {

    private static final double REJECTED_INCOME_THRESHOLD = 1000.0;
    private static final double MANUAL_REVIEW_INCOME_THRESHOLD = 2500.0;
    private static final String HIGH_RISK_COUNTRY_CODE = "XZ";
    private static final int HIGH_RISK_DOCUMENT_LENGTH = 6;

    private final OnboardingRepository onboardingRepository;

    public CreateOnboardingResponse createOnboarding(CreateOnboardingRequest request) {
        String onboardingId = UUID.randomUUID().toString();
        OnboardingDecision decision = decide(request);
        log.info(
                "onboarding.create.start onboardingId={} email={} countryCode={}",
                onboardingId,
                request.email(),
                request.countryCode()
        );

        CustomerOnboardingRecord record = new CustomerOnboardingRecord(
                onboardingId,
                request.fullName(),
                request.email(),
                request.phoneNumber(),
                request.countryCode(),
                request.documentId(),
                request.monthlyIncome(),
                decision.status(),
                decision.reason(),
                Instant.now()
        );

        onboardingRepository.save(record);
        log.info(
                "onboarding.create.complete onboardingId={} status={} reason={}",
                onboardingId,
                decision.status(),
                decision.reason()
        );

        return new CreateOnboardingResponse(record.onboardingId(), record.status(), record.decisionReason());
    }

    public OnboardingDetailsResponse getOnboarding(String onboardingId) {
        log.info("onboarding.fetch.request onboardingId={}", onboardingId);
        CustomerOnboardingRecord record = onboardingRepository.findById(onboardingId)
                .orElseThrow(() -> new OnboardingNotFoundException(onboardingId));

        log.info("onboarding.fetch.success onboardingId={} status={}", onboardingId, record.status());
        return new OnboardingDetailsResponse(
                record.onboardingId(),
                record.fullName(),
                record.email(),
                record.phoneNumber(),
                record.countryCode(),
                record.documentId(),
                record.monthlyIncome(),
                record.status(),
                record.decisionReason(),
                record.createdAt()
        );
    }

    private OnboardingDecision decide(CreateOnboardingRequest request) {
        String normalizedCountry = request.countryCode().toUpperCase(Locale.ROOT);
        String normalizedDoc = request.documentId().replaceAll("\\s+", "");

        if (request.monthlyIncome() < REJECTED_INCOME_THRESHOLD) {
            return new OnboardingDecision(OnboardingStatus.REJECTED, "Income below minimum threshold");
        }

        if (request.monthlyIncome() < MANUAL_REVIEW_INCOME_THRESHOLD) {
            return new OnboardingDecision(OnboardingStatus.MANUAL_REVIEW, "Income requires additional review");
        }

        if (HIGH_RISK_COUNTRY_CODE.equals(normalizedCountry)) {
            return new OnboardingDecision(OnboardingStatus.MANUAL_REVIEW, "Country flagged for enhanced due diligence");
        }

        if (normalizedDoc.length() < HIGH_RISK_DOCUMENT_LENGTH) {
            return new OnboardingDecision(OnboardingStatus.MANUAL_REVIEW, "Document quality check failed");
        }

        return new OnboardingDecision(OnboardingStatus.APPROVED, "Automated onboarding checks passed");
    }

    private record OnboardingDecision(OnboardingStatus status, String reason) {
    }
}
