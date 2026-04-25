package com.rbs.customer.onboarding.onboarding_service.observability;

public interface FailureSignalPublisher {
    void publish(FailureSignal signal);
}
