package com.rbs.customer.onboarding.onboarding_service.observability;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NoOpFailureSignalPublisher implements FailureSignalPublisher {
    @Override
    public void publish(FailureSignal signal) {
        log.warn("Failure signal captured for future MCP publishing: {}", signal);
    }
}
