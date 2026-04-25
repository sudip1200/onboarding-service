package com.rbs.customer.onboarding.onboarding_service.integration.mcp;

import com.rbs.customer.onboarding.onboarding_service.incident.ErrorCriticality;
import com.rbs.customer.onboarding.onboarding_service.observability.FailureSignal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "integrations.mcp", name = "enabled", havingValue = "true")
public class RestMcpIncidentClient implements McpIncidentClient {

    private final McpProperties properties;

    @Override
    public void publish(FailureSignal signal, ErrorCriticality criticality, Map<String, Object> enrichment) {
        Map<String, Object> payload = Map.of(
                "timestamp", Instant.now().toString(),
                "criticality", criticality.name(),
                "signal", Map.of(
                        "correlationId", signal.correlationId(),
                        "endpoint", signal.endpoint(),
                        "errorCode", signal.errorCode(),
                        "statusCode", signal.statusCode(),
                        "failureType", signal.failureType(),
                        "message", safe(signal.message()),
                        "eventTimestamp", signal.timestamp().toString()
                ),
                "enrichment", enrichment
        );

        try {
            RestClient.create()
                    .post()
                    .uri(properties.getBaseUrl() + properties.getIncidentPath())
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
            log.info(
                    "mcp.publish.success correlationId={} criticality={}",
                    signal.correlationId(),
                    criticality
            );
        } catch (Exception ex) {
            log.error(
                    "mcp.publish.failed correlationId={} criticality={} message={}",
                    signal.correlationId(),
                    criticality,
                    ex.getMessage()
            );
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
