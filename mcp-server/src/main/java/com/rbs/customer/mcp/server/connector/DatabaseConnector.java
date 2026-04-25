package com.rbs.customer.mcp.server.connector;

import java.util.Map;

public interface DatabaseConnector {
    Map<String, Object> fetchDiagnostics(String correlationId);
}
