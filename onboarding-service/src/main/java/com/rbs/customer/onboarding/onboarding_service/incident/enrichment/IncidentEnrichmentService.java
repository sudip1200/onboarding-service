package com.rbs.customer.onboarding.onboarding_service.incident.enrichment;

import com.rbs.customer.onboarding.onboarding_service.incident.ErrorCriticality;
import com.rbs.customer.onboarding.onboarding_service.observability.FailureSignal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class IncidentEnrichmentService {

    private final List<IncidentContextEnricher> enrichers;

    public Map<String, Object> enrich(FailureSignal signal, ErrorCriticality criticality) {
        Map<String, Object> payload = new LinkedHashMap<>();
        for (IncidentContextEnricher enricher : enrichers) {
            payload.put(enricher.source(), enricher.enrich(signal, criticality));
        }
        return payload;
    }
}
