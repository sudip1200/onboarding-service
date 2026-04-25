package com.rbs.customer.onboarding.onboarding_service.incident;

import com.rbs.customer.onboarding.onboarding_service.observability.FailureSignal;
import org.springframework.stereotype.Component;

@Component
public class ErrorCriticalityClassifier {

    public ErrorCriticality classify(FailureSignal signal) {
        if (signal.statusCode() >= 500 || "INTERNAL_SERVER_ERROR".equals(signal.errorCode())) {
            return ErrorCriticality.CRITICAL;
        }

        if (signal.statusCode() == 404 || "ONBOARDING_NOT_FOUND".equals(signal.errorCode())) {
            return ErrorCriticality.HIGH;
        }

        if (signal.statusCode() == 400 || "VALIDATION_ERROR".equals(signal.errorCode())) {
            return ErrorCriticality.MEDIUM;
        }

        return ErrorCriticality.LOW;
    }
}
