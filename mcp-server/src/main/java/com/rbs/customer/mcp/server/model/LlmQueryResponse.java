package com.rbs.customer.mcp.server.model;

import java.time.Instant;
import java.util.Map;

public record LlmQueryResponse(
        String provider,
        String model,
        String answer,
        boolean fallback,
        Instant generatedAt,
        Map<String, Object> metadata
) {
}
