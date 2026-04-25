package com.rbs.customer.mcp.server.connector.impl;

import com.rbs.customer.mcp.server.config.McpConnectorProperties;
import com.rbs.customer.mcp.server.connector.CloudWatchConnector;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "mcp.connectors.cloudwatch", name = "enabled", havingValue = "false", matchIfMissing = true)
public class NoOpCloudWatchConnector implements CloudWatchConnector {

    private final McpConnectorProperties properties;

    @Override
    public List<String> fetchLogs(String correlationId) {
        String logGroup = properties.getCloudwatch().getLogGroup() == null
                ? "not-configured"
                : properties.getCloudwatch().getLogGroup();
        return List.of(
                "cloudwatch.enabled=" + properties.getCloudwatch().isEnabled(),
                "cloudwatch.logGroup=" + logGroup,
                "cloudwatch.sample=stub log for correlationId=" + correlationId
        );
    }
}
