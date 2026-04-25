package com.rbs.customer.mcp.server.model;

import java.util.List;
import java.util.Map;

public record RcaResult(
        String summary,
        String probableRootCause,
        List<String> recommendedActions,
        Map<String, Object> relatedArtifacts
) {
}
