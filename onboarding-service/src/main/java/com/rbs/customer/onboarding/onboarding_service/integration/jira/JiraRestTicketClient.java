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

    private static final String ATLASSIAN_API_BASE = "https://api.atlassian.com/ex/jira";
    private static final int JIRA_SUMMARY_MAX = 255;
    private final JiraProperties properties;

    @Override
    public void createTicket(FailureSignal signal, ErrorCriticality criticality) {
        JiraRequestTarget target = resolveTarget();
        if (target == null) {
            System.out.println("JIRA_CREATE_FAILED correlationId=" + signal.correlationId()
                    + " reason=configuration_incomplete");
            log.error(
                    "jira.ticket.failed correlationId={} criticality={} errorCode={} message={}",
                    signal.correlationId(),
                    criticality,
                    signal.errorCode(),
                    "Jira configuration is incomplete. Provide JIRA_CLOUD_ID + JIRA_API_TOKEN for scoped auth, or JIRA_BASE_URL + JIRA_EMAIL + JIRA_API_TOKEN for basic auth."
            );
            return;
        }

        String summary = buildSummary(signal, criticality);
        String description = buildDescription(signal, criticality);

        Map<String, Object> body = Map.of(
                "fields", Map.of(
                        "project", Map.of("key", properties.getProjectKey()),
                        "summary", summary,
                        "description", toAdf(description),
                        "issuetype", Map.of("name", properties.getIssueType())
                )
        );

        try {
            Map<String, Object> response = RestClient.create()
                    .post()
                    .uri(target.url())
                    .header("Authorization", target.authorizationHeader())
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            String issueKey = response == null ? "UNKNOWN" : String.valueOf(response.getOrDefault("key", "UNKNOWN"));
            System.out.println("JIRA_CREATE_SUCCESS correlationId=" + signal.correlationId()
                    + " jiraKey=" + issueKey);

            log.warn(
                    "jira.ticket.created correlationId={} criticality={} errorCode={} jiraKey={}",
                    signal.correlationId(),
                    criticality,
                    signal.errorCode(),
                    issueKey
            );
        } catch (Exception ex) {
            System.out.println("JIRA_CREATE_FAILED correlationId=" + signal.correlationId()
                    + " reason=" + safe(ex.getMessage()));
            log.error(
                    "jira.ticket.failed correlationId={} criticality={} errorCode={} message={}",
                    signal.correlationId(),
                    criticality,
                    signal.errorCode(),
                    ex.getMessage()
            );
        }
    }

    private String buildSummary(FailureSignal signal, ErrorCriticality criticality) {
        String summary = String.format(
                "[%s] onboarding-service | %s | %s | %s",
                criticality,
                signal.errorCode(),
                signal.endpoint(),
                signal.correlationId()
        );
        if (summary.length() <= JIRA_SUMMARY_MAX) {
            return summary;
        }
        return summary.substring(0, JIRA_SUMMARY_MAX - 3) + "...";
    }

    private String buildDescription(FailureSignal signal, ErrorCriticality criticality) {
        String logLine = String.format(
                "request.failed code=%s status=%s correlationId=%s endpoint=%s type=%s message=%s",
                safe(signal.errorCode()),
                signal.statusCode(),
                safe(signal.correlationId()),
                safe(signal.endpoint()),
                safe(signal.failureType()),
                safe(signal.message())
        );

        return String.join(
                "\n",
                "Auto-created from onboarding-service incident automation",
                "time=" + Instant.now(),
                "service=onboarding-service",
                "criticality=" + criticality,
                "correlationId=" + signal.correlationId(),
                "endpoint=" + signal.endpoint(),
                "statusCode=" + signal.statusCode(),
                "errorCode=" + signal.errorCode(),
                "failureType=" + signal.failureType(),
                "message=" + signal.message(),
                "",
                "logSnapshot:",
                logLine,
                "incident.classified correlationId=" + signal.correlationId()
                        + " errorCode=" + signal.errorCode()
                        + " statusCode=" + signal.statusCode()
                        + " criticality=" + criticality,
                "",
                "investigationHints:",
                "Search logs with correlationId=\"" + safe(signal.correlationId()) + "\"",
                "Check endpoint timeline around eventTimestamp=" + signal.timestamp()
        );
    }

    private JiraRequestTarget resolveTarget() {
        String token = safe(properties.getApiToken());
        String cloudId = safe(properties.getCloudId());
        String email = safe(properties.getEmail());
        String path = normalizePath(properties.getCreateIssuePath(), "/rest/api/3/issue");

        if (!cloudId.isEmpty() && !token.isEmpty()) {
            String url = ATLASSIAN_API_BASE + "/" + cloudId + path;
            if (!email.isEmpty()) {
                String authValue = Base64.getEncoder().encodeToString((email + ":" + token).getBytes(StandardCharsets.UTF_8));
                return new JiraRequestTarget(url, "Basic " + authValue);
            }
            return new JiraRequestTarget(url, "Bearer " + token);
        }

        String baseUrl = trimTrailingSlash(properties.getBaseUrl());
        if (!baseUrl.isEmpty() && !email.isEmpty() && !token.isEmpty()) {
            String authValue = Base64.getEncoder().encodeToString((email + ":" + token).getBytes(StandardCharsets.UTF_8));
            return new JiraRequestTarget(baseUrl + path, "Basic " + authValue);
        }

        return null;
    }

    private String trimTrailingSlash(String value) {
        String safeValue = safe(value);
        if (safeValue.endsWith("/")) {
            return safeValue.substring(0, safeValue.length() - 1);
        }
        return safeValue;
    }

    private String normalizePath(String value, String fallback) {
        String safeValue = safe(value);
        if (safeValue.isEmpty()) {
            return fallback;
        }
        return safeValue.startsWith("/") ? safeValue : "/" + safeValue;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private Map<String, Object> toAdf(String text) {
        String[] lines = safe(text).split("\\R");
        Object[] paragraphs = java.util.Arrays.stream(lines)
                .filter(line -> !line.isBlank())
                .map(line -> Map.<String, Object>of(
                        "type", "paragraph",
                        "content", java.util.List.of(
                                Map.of(
                                        "type", "text",
                                        "text", line
                                )
                        )
                ))
                .toArray();

        if (paragraphs.length == 0) {
            paragraphs = new Object[] {
                    Map.of(
                            "type", "paragraph",
                            "content", java.util.List.of(
                                    Map.of("type", "text", "text", "Auto-created by onboarding-service")
                            )
                    )
            };
        }

        return Map.of(
                "type", "doc",
                "version", 1,
                "content", java.util.List.of(paragraphs)
        );
    }

    private record JiraRequestTarget(String url, String authorizationHeader) {
    }
}
