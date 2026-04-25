package com.rbs.customer.onboarding.onboarding_service.api;

import com.rbs.customer.onboarding.onboarding_service.model.CreateOnboardingRequest;
import com.rbs.customer.onboarding.onboarding_service.model.CreateOnboardingResponse;
import com.rbs.customer.onboarding.onboarding_service.model.OnboardingDetailsResponse;
import com.rbs.customer.onboarding.onboarding_service.service.CustomerOnboardingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/onboardings")
@RequiredArgsConstructor
public class CustomerOnboardingController {

    private final CustomerOnboardingService onboardingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateOnboardingResponse createOnboarding(@Valid @RequestBody CreateOnboardingRequest request) {
        return onboardingService.createOnboarding(request);
    }

    @GetMapping("/{onboardingId}")
    public OnboardingDetailsResponse getOnboarding(@PathVariable String onboardingId) {
        return onboardingService.getOnboarding(onboardingId);
    }
}
