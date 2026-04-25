package com.rbs.customer.mcp.server.connector;

import java.util.List;

public interface CloudWatchConnector {
    List<String> fetchLogs(String correlationId);
}
