package com.rbs.customer.mcp.server.connector.impl;

import com.rbs.customer.mcp.server.config.McpConnectorProperties;
import com.rbs.customer.mcp.server.connector.DatabaseConnector;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class NoOpDatabaseConnector implements DatabaseConnector {

    private final McpConnectorProperties properties;

    @Override
    public Map<String, Object> fetchDiagnostics(String correlationId) {
        return Map.of(
                "enabled", properties.getDatabase().isEnabled(),
                "jdbcUrl", safe(properties.getDatabase().getJdbcUrl()),
                "health", "stub-ok",
                "correlationId", correlationId
        );
    }

    private String safe(String value) {
        return value == null ? "not-configured" : value;
    }
}
