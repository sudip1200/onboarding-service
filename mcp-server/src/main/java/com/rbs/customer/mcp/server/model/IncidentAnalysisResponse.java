package com.rbs.customer.mcp.server.model;

public record IncidentAnalysisResponse(
        String incidentId,
        String processedAt,
        String jiraTicketKey,
        RcaResult rca
) {
}
