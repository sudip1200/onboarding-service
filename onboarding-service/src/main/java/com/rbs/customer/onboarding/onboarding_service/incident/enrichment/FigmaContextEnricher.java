package com.rbs.customer.onboarding.onboarding_service.incident.enrichment;

import com.rbs.customer.onboarding.onboarding_service.incident.ErrorCriticality;
import com.rbs.customer.onboarding.onboarding_service.integration.figma.FigmaProperties;
import com.rbs.customer.onboarding.onboarding_service.observability.FailureSignal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class FigmaContextEnricher implements IncidentContextEnricher {

    private final FigmaProperties figmaProperties;

    @Override
    public String source() {
        return "figma";
    }

    @Override
    public Map<String, Object> enrich(FailureSignal signal, ErrorCriticality criticality) {
        return Map.of(
                "enabled", figmaProperties.isEnabled(),
                "projectName", safe(figmaProperties.getProjectName()),
                "fileKey", safe(figmaProperties.getFileKey())
        );
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "not-configured" : value;
    }
}
