package com.rbs.customer.onboarding.onboarding_service.incident;

import com.rbs.customer.onboarding.onboarding_service.incident.enrichment.IncidentEnrichmentService;
import com.rbs.customer.onboarding.onboarding_service.integration.jira.JiraTicketClient;
import com.rbs.customer.onboarding.onboarding_service.integration.mcp.McpIncidentClient;
import com.rbs.customer.onboarding.onboarding_service.observability.FailureSignal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class IncidentAutomationService {

    private final ErrorCriticalityClassifier criticalityClassifier;
    private final IncidentAutomationProperties properties;
    private final JiraTicketClient jiraTicketClient;
    private final IncidentEnrichmentService incidentEnrichmentService;
    private final McpIncidentClient mcpIncidentClient;

    public void process(FailureSignal signal) {
        ErrorCriticality criticality = criticalityClassifier.classify(signal);
        Map<String, Object> enrichment = incidentEnrichmentService.enrich(signal, criticality);
        log.warn(
                "incident.classified correlationId={} errorCode={} statusCode={} criticality={}",
                signal.correlationId(),
                signal.errorCode(),
                signal.statusCode(),
                criticality
        );
        mcpIncidentClient.publish(signal, criticality, enrichment);

        if (criticality.ordinal() >= properties.getJiraThreshold().ordinal()) {
            jiraTicketClient.createTicket(signal, criticality);
        }
    }
}
