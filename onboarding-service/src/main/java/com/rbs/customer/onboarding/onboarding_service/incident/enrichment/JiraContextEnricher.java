package com.rbs.customer.onboarding.onboarding_service.incident.enrichment;

import com.rbs.customer.onboarding.onboarding_service.incident.ErrorCriticality;
import com.rbs.customer.onboarding.onboarding_service.integration.jira.JiraProperties;
import com.rbs.customer.onboarding.onboarding_service.observability.FailureSignal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class JiraContextEnricher implements IncidentContextEnricher {

    private final JiraProperties jiraProperties;

    @Override
    public String source() {
        return "jira";
    }

    @Override
    public Map<String, Object> enrich(FailureSignal signal, ErrorCriticality criticality) {
        return Map.of(
                "enabled", jiraProperties.isEnabled(),
                "projectKey", jiraProperties.getProjectKey(),
                "issueType", jiraProperties.getIssueType(),
                "ticketSummaryHint", "[" + criticality + "] onboarding-service error " + signal.errorCode()
        );
    }
}
