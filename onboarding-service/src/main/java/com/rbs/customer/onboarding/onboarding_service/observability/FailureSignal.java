package com.rbs.customer.onboarding.onboarding_service.observability;

import java.time.Instant;

public record FailureSignal(
        String correlationId,
        String endpoint,
        String errorCode,
        int statusCode,
        String failureType,
        String message,
        Instant timestamp
) {
}
