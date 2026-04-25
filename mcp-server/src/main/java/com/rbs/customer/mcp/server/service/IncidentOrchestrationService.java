package com.rbs.customer.mcp.server.service;

import com.rbs.customer.mcp.server.connector.CloudWatchConnector;
import com.rbs.customer.mcp.server.connector.DatabaseConnector;
import com.rbs.customer.mcp.server.connector.FigmaConnector;
import com.rbs.customer.mcp.server.connector.GitConnector;
import com.rbs.customer.mcp.server.connector.JiraConnector;
import com.rbs.customer.mcp.server.connector.S3Connector;
import com.rbs.customer.mcp.server.model.IncidentAnalysisResponse;
import com.rbs.customer.mcp.server.model.IncidentPayload;
import com.rbs.customer.mcp.server.model.RcaResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class IncidentOrchestrationService {

    private final JiraConnector jiraConnector;
    private final GitConnector gitConnector;
    private final CloudWatchConnector cloudWatchConnector;
    private final S3Connector s3Connector;
    private final FigmaConnector figmaConnector;
    private final DatabaseConnector databaseConnector;

    public IncidentAnalysisResponse processIncident(IncidentPayload payload) {
        String correlationId = payload.signal().correlationId();
        Map<String, Object> context = Map.of(
                "git", gitConnector.fetchRecentCommitContext(correlationId),
                "cloudwatch", cloudWatchConnector.fetchLogs(correlationId),
                "s3", s3Connector.fetchArtifacts(correlationId),
                "figma", figmaConnector.fetchDesignContext(correlationId),
                "database", databaseConnector.fetchDiagnostics(correlationId),
                "serviceEnrichment", payload.enrichment()
        );

        RcaResult rcaResult = new RcaResult(
                "Incident correlated across logs, commits, design, and data diagnostics",
                "Probable issue around endpoint " + payload.signal().endpoint() + " with error " + payload.signal().errorCode(),
                List.of(
                        "Validate latest deployment and commit diff for impacted endpoint",
                        "Check CloudWatch logs for repeated failures by correlationId",
                        "Review design-to-API field mapping for payload validation mismatch",
                        "Add/adjust defensive validation or fallback in service path"
                ),
                context
        );

        String jiraTicketKey = "NOT_CREATED";
        if (isCritical(payload.criticality())) {
            jiraTicketKey = jiraConnector.createOrUpdateIncidentTicket(payload, rcaResult);
        }

        String incidentId = UUID.randomUUID().toString();
        log.info(
                "mcp.incident.processed incidentId={} correlationId={} criticality={} jiraTicketKey={}",
                incidentId,
                correlationId,
                payload.criticality(),
                jiraTicketKey
        );
        return new IncidentAnalysisResponse(incidentId, Instant.now().toString(), jiraTicketKey, rcaResult);
    }

    private boolean isCritical(String criticality) {
        return "CRITICAL".equalsIgnoreCase(criticality);
    }
}
