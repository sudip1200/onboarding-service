package com.rbs.customer.onboarding.onboarding_service.incident.enrichment;

import com.rbs.customer.onboarding.onboarding_service.incident.ErrorCriticality;
import com.rbs.customer.onboarding.onboarding_service.observability.FailureSignal;

import java.util.Map;

public interface IncidentContextEnricher {
    String source();

    Map<String, Object> enrich(FailureSignal signal, ErrorCriticality criticality);
}
