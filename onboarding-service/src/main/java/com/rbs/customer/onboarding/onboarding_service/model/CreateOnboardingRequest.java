package com.rbs.customer.onboarding.onboarding_service.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record CreateOnboardingRequest(
        @NotBlank(message = "fullName is required")
        String fullName,
        @Email(message = "email must be valid")
        @NotBlank(message = "email is required")
        String email,
        @Pattern(regexp = "\\d{10}", message = "phoneNumber must be a 10 digit number")
        String phoneNumber,
        @NotBlank(message = "countryCode is required")
        String countryCode,
        @NotBlank(message = "documentId is required")
        String documentId,
        @NotNull(message = "monthlyIncome is required")
        @Positive(message = "monthlyIncome must be greater than 0")
        Double monthlyIncome
) {
}
