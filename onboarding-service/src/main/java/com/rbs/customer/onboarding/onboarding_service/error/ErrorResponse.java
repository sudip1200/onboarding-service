package com.rbs.customer.onboarding.onboarding_service.error;

import java.time.Instant;

public record ErrorResponse(
        String errorCode,
        String message,
        String correlationId,
        Instant timestamp
) {
}
