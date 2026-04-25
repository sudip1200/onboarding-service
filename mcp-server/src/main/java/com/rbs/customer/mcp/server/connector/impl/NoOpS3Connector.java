package com.rbs.customer.mcp.server.connector.impl;

import com.rbs.customer.mcp.server.config.McpConnectorProperties;
import com.rbs.customer.mcp.server.connector.S3Connector;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NoOpS3Connector implements S3Connector {

    private final McpConnectorProperties properties;

    @Override
    public List<String> fetchArtifacts(String correlationId) {
        return List.of(
                "s3.enabled=" + properties.getS3().isEnabled(),
                "s3.bucket=" + safe(properties.getS3().getBucket()),
                "s3.prefix=" + safe(properties.getS3().getPrefix()),
                "s3.sampleArtifact=" + correlationId + ".json"
        );
    }

    private String safe(String value) {
        return value == null ? "not-configured" : value;
    }
}
