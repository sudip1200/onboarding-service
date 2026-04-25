package com.rbs.customer.mcp.server.connector;

import java.util.List;

public interface S3Connector {
    List<String> fetchArtifacts(String correlationId);
}
