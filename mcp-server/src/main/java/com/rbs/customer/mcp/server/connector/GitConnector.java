package com.rbs.customer.mcp.server.connector;

import java.util.Map;

public interface GitConnector {
    Map<String, Object> fetchRecentCommitContext(String correlationId);
}
