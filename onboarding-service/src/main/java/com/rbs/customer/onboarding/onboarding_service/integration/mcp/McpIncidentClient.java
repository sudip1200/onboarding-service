package com.rbs.customer.onboarding.onboarding_service.integration.mcp;

import com.rbs.customer.onboarding.onboarding_service.incident.ErrorCriticality;
import com.rbs.customer.onboarding.onboarding_service.observability.FailureSignal;

import java.util.Map;

public interface McpIncidentClient {
    void publish(FailureSignal signal, ErrorCriticality criticality, Map<String, Object> enrichment);
}
