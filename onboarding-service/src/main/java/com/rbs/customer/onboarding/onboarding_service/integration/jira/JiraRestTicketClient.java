package com.rbs.customer.onboarding.onboarding_service.integration.jira;

import com.rbs.customer.onboarding.onboarding_service.incident.ErrorCriticality;
import com.rbs.customer.onboarding.onboarding_service.observability.FailureSignal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "integrations.jira", name = "enabled", havingValue = "true")
public class JiraRestTicketClient implements JiraTicketClient {

    private final JiraProperties properties;

    @Override
    public void createTicket(FailureSignal signal, ErrorCriticality criticality) {
        String authValue = Base64.getEncoder().encodeToString(
                (properties.getEmail() + ":" + properties.getApiToken()).getBytes(StandardCharsets.UTF_8)
        );

        String summary = "[" + criticality + "] onboarding-service error " + signal.errorCode();
        String description = String.join(
                "\n",
                "Auto-created from onboarding-service",
                "time=" + Instant.now(),
                "correlationId=" + signal.correlationId(),
                "endpoint=" + signal.endpoint(),
                "statusCode=" + signal.statusCode(),
                "errorCode=" + signal.errorCode(),
                "failureType=" + signal.failureType(),
                "message=" + signal.message()
        );

        Map<String, Object> body = Map.of(
                "fields", Map.of(
                        "project", Map.of("key", properties.getProjectKey()),
                        "summary", summary,
                        "description", description,
                        "issuetype", Map.of("name", properties.getIssueType())
                )
        );

        try {
            RestClient.create()
                    .post()
                    .uri(properties.getBaseUrl() + properties.getCreateIssuePath())
                    .header("Authorization", "Basic " + authValue)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();

            log.warn(
                    "jira.ticket.created correlationId={} criticality={} errorCode={}",
                    signal.correlationId(),
                    criticality,
                    signal.errorCode()
            );
        } catch (Exception ex) {
            log.error(
                    "jira.ticket.failed correlationId={} criticality={} errorCode={} message={}",
                    signal.correlationId(),
                    criticality,
                    signal.errorCode(),
                    ex.getMessage()
            );
        }
    }
}
