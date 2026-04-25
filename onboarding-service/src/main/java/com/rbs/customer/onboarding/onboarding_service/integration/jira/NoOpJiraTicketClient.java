package com.rbs.customer.onboarding.onboarding_service.integration.jira;

import com.rbs.customer.onboarding.onboarding_service.incident.ErrorCriticality;
import com.rbs.customer.onboarding.onboarding_service.observability.FailureSignal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@ConditionalOnProperty(prefix = "integrations.jira", name = "enabled", havingValue = "false", matchIfMissing = true)
public class NoOpJiraTicketClient implements JiraTicketClient {
    @Override
    public void createTicket(FailureSignal signal, ErrorCriticality criticality) {
        log.info(
                "jira.ticket.skipped enabled=false correlationId={} criticality={} errorCode={}",
                signal.correlationId(),
                criticality,
                signal.errorCode()
        );
    }
}
