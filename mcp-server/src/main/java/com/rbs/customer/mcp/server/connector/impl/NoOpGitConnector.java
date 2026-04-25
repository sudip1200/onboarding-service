package com.rbs.customer.mcp.server.connector.impl;

import com.rbs.customer.mcp.server.config.McpConnectorProperties;
import com.rbs.customer.mcp.server.connector.GitConnector;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class NoOpGitConnector implements GitConnector {

    private final McpConnectorProperties properties;

    @Override
    public Map<String, Object> fetchRecentCommitContext(String correlationId) {
        return Map.of(
                "enabled", properties.getGit().isEnabled(),
                "provider", safe(properties.getGit().getProvider()),
                "repo", safe(properties.getGit().getRepo()),
                "latestCommit", "stub-commit",
                "correlationId", correlationId
        );
    }

    private String safe(String value) {
        return value == null ? "not-configured" : value;
    }
}
