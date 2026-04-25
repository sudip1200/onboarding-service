package com.rbs.customer.onboarding.onboarding_service.integration.jira;

import com.rbs.customer.onboarding.onboarding_service.incident.ErrorCriticality;
import com.rbs.customer.onboarding.onboarding_service.observability.FailureSignal;

public interface JiraTicketClient {
    void createTicket(FailureSignal signal, ErrorCriticality criticality);
}
