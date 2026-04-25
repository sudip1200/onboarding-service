package com.rbs.customer.onboarding.onboarding_service.incident.enrichment;

import com.rbs.customer.onboarding.onboarding_service.incident.ErrorCriticality;
import com.rbs.customer.onboarding.onboarding_service.observability.FailureSignal;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GitContextEnricher implements IncidentContextEnricher {

    @Override
    public String source() {
        return "git";
    }

    @Override
    public Map<String, Object> enrich(FailureSignal signal, ErrorCriticality criticality) {
        return Map.of(
                "commit", env("GIT_COMMIT", "unknown"),
                "branch", env("GIT_BRANCH", "unknown"),
                "repo", env("GIT_REPO", "unknown")
        );
    }

    private String env(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }
}
