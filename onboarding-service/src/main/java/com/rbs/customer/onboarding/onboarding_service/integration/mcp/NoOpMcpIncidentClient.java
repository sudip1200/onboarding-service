package com.rbs.customer.onboarding.onboarding_service.integration.mcp;

import com.rbs.customer.onboarding.onboarding_service.incident.ErrorCriticality;
import com.rbs.customer.onboarding.onboarding_service.observability.FailureSignal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
@ConditionalOnProperty(prefix = "integrations.mcp", name = "enabled", havingValue = "false", matchIfMissing = true)
public class NoOpMcpIncidentClient implements McpIncidentClient {
    @Override
    public void publish(FailureSignal signal, ErrorCriticality criticality, Map<String, Object> enrichment) {
        log.info(
                "mcp.publish.skipped enabled=false correlationId={} criticality={} enrichmentSources={}",
                signal.correlationId(),
                criticality,
                enrichment.keySet()
        );
    }
}
