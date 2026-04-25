package com.rbs.customer.mcp.server.connector;

import java.util.Map;

public interface FigmaConnector {
    Map<String, Object> fetchDesignContext(String correlationId);
}
