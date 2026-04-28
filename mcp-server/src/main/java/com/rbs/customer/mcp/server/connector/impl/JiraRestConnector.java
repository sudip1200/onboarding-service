package com.rbs.customer.mcp.server.connector.impl;

import com.rbs.customer.mcp.server.config.McpConnectorProperties;
import com.rbs.customer.mcp.server.connector.JiraConnector;
import com.rbs.customer.mcp.server.model.IncidentPayload;
import com.rbs.customer.mcp.server.model.RcaResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "mcp.connectors.jira", name = "enabled", havingValue = "true")
public class JiraRestConnector implements JiraConnector {

    private static final String ATLASSIAN_API_BASE = "https://api.atlassian.com/ex/jira";
    private static final String ISSUE_FIELDS = "summary,description,status,priority,labels,issuetype";
    private final McpConnectorProperties properties;

    @Override
    @SuppressWarnings("unchecked")
    public String createOrUpdateIncidentTicket(IncidentPayload payload, RcaResult rcaResult) {
        JiraRequestTarget target = resolveTarget(properties.getJira().getCreateIssuePath(), "/rest/api/3/issue");
        if (target == null) {
            log.error(
                    "jira.connector.failed correlationId={} message={}",
                    payload.signal().correlationId(),
                    "Jira configuration is incomplete. Provide MCP_JIRA_CLOUD_ID + MCP_JIRA_API_TOKEN for scoped auth, or MCP_JIRA_BASE_URL + MCP_JIRA_EMAIL + MCP_JIRA_API_TOKEN for basic auth."
            );
            return "FAILED";
        }
        String summary = "[" + payload.criticality() + "] onboarding incident " + payload.signal().errorCode();

        Map<String, Object> request = Map.of(
                "fields", Map.of(
                        "project", Map.of("key", properties.getJira().getProjectKey()),
                        "summary", summary,
                        "description", buildDescription(payload, rcaResult),
                        "issuetype", Map.of("name", "Bug")
                )
        );

        try {
            Map<String, Object> response = RestClient.create()
                    .post()
                    .uri(target.url())
                    .header("Authorization", target.authorizationHeader())
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .body(request)
                    .retrieve()
                    .body(Map.class);

            String key = response == null ? "UNKNOWN" : String.valueOf(response.getOrDefault("key", "UNKNOWN"));
            log.warn("jira.connector.created key={} correlationId={}", key, payload.signal().correlationId());
            return key;
        } catch (Exception ex) {
            log.error(
                    "jira.connector.failed correlationId={} message={}",
                    payload.signal().correlationId(),
                    ex.getMessage()
            );
            return "FAILED";
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> fetchTicketByKey(String ticketKey) {
        String safeTicketKey = safe(ticketKey);
        if (safeTicketKey.isEmpty()) {
            return Map.of("status", "INVALID_REQUEST", "message", "ticketKey is required");
        }

        JiraRequestTarget target = resolveTarget(properties.getJira().getGetIssuePath(), "/rest/api/3/issue");
        if (target == null) {
            return Map.of(
                    "key", safeTicketKey,
                    "status", "FAILED",
                    "message", "Jira configuration is incomplete."
            );
        }

        String encodedKey = URLEncoder.encode(safeTicketKey, StandardCharsets.UTF_8);
        String url = target.url() + "/" + encodedKey + "?fields=" + ISSUE_FIELDS;
        try {
            Map<String, Object> response = RestClient.create()
                    .get()
                    .uri(url)
                    .header("Authorization", target.authorizationHeader())
                    .header("Accept", "application/json")
                    .retrieve()
                    .body(Map.class);

            if (response == null) {
                return Map.of(
                        "key", safeTicketKey,
                        "status", "EMPTY",
                        "message", "No Jira response received."
                );
            }
            return response;
        } catch (Exception ex) {
            log.error("jira.connector.fetch.failed ticketKey={} message={}", safeTicketKey, ex.getMessage());
            return Map.of(
                    "key", safeTicketKey,
                    "status", "FAILED",
                    "message", safe(ex.getMessage())
            );
        }
    }

    private JiraRequestTarget resolveTarget(String configuredPath, String fallbackPath) {
        McpConnectorProperties.Jira jira = properties.getJira();
        String token = safe(jira.getApiToken());
        String cloudId = safe(jira.getCloudId());
        String email = safe(jira.getEmail());
        String path = normalizePath(configuredPath, fallbackPath);

        if (!cloudId.isEmpty() && !token.isEmpty()) {
            String url = ATLASSIAN_API_BASE + "/" + cloudId + path;
            if (!email.isEmpty()) {
                return new JiraRequestTarget(url, "Basic " + basicAuth(email, token));
            }
            return new JiraRequestTarget(url, "Bearer " + token);
        }

        String baseUrl = trimTrailingSlash(jira.getBaseUrl());
        if (!baseUrl.isEmpty() && !email.isEmpty() && !token.isEmpty()) {
            return new JiraRequestTarget(baseUrl + path, "Basic " + basicAuth(email, token));
        }
        return null;
    }

    private String buildDescription(IncidentPayload payload, RcaResult rcaResult) {
        Map<String, Object> lines = new LinkedHashMap<>();
        lines.put("correlationId", payload.signal().correlationId());
        lines.put("endpoint", payload.signal().endpoint());
        lines.put("errorCode", payload.signal().errorCode());
        lines.put("statusCode", payload.signal().statusCode());
        lines.put("failureType", payload.signal().failureType());
        lines.put("message", safe(payload.signal().message()));
        lines.put("rcaSummary", rcaResult.summary());
        lines.put("probableRootCause", rcaResult.probableRootCause());
        lines.put("recommendedActions", rcaResult.recommendedActions());
        return lines.toString();
    }

    private String basicAuth(String email, String token) {
        String raw = safe(email) + ":" + safe(token);
        return Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    private String trimTrailingSlash(String value) {
        String safeValue = safe(value);
        return safeValue.endsWith("/") ? safeValue.substring(0, safeValue.length() - 1) : safeValue;
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

    private record JiraRequestTarget(String url, String authorizationHeader) {
    }
}
