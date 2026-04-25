package com.rbs.customer.mcp.server.connector.impl;

import com.rbs.customer.mcp.server.config.McpConnectorProperties;
import com.rbs.customer.mcp.server.connector.FigmaConnector;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class NoOpFigmaConnector implements FigmaConnector {

    private final McpConnectorProperties properties;

    @Override
    public Map<String, Object> fetchDesignContext(String correlationId) {
        return Map.of(
                "enabled", properties.getFigma().isEnabled(),
                "projectName", safe(properties.getFigma().getProjectName()),
                "fileKey", safe(properties.getFigma().getFileKey()),
                "sampleFlow", "onboarding-form"
        );
    }

    private String safe(String value) {
        return value == null ? "not-configured" : value;
    }
}
