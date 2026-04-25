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
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "mcp.connectors.jira", name = "enabled", havingValue = "true")
public class JiraRestConnector implements JiraConnector {

    private final McpConnectorProperties properties;

    @Override
    @SuppressWarnings("unchecked")
    public String createOrUpdateIncidentTicket(IncidentPayload payload, RcaResult rcaResult) {
        String auth = basicAuth(properties.getJira().getEmail(), properties.getJira().getApiToken());
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
                    .uri(trimTrailingSlash(properties.getJira().getBaseUrl()) + "/rest/api/2/issue")
                    .header("Authorization", "Basic " + auth)
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

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
