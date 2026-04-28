package com.rbs.customer.mcp.server.model;

import java.util.Map;

public record LlmQueryRequest(
        String question,
        String ticket,
        String jiraId,
        String code,
        String model,
        String correlationId,
        Map<String, Object> context
) {
}
