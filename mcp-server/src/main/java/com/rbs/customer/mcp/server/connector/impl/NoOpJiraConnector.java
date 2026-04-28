package com.rbs.customer.mcp.server.connector.impl;

import com.rbs.customer.mcp.server.connector.JiraConnector;
import com.rbs.customer.mcp.server.model.IncidentPayload;
import com.rbs.customer.mcp.server.model.RcaResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
@ConditionalOnProperty(prefix = "mcp.connectors.jira", name = "enabled", havingValue = "false", matchIfMissing = true)
public class NoOpJiraConnector implements JiraConnector {
    @Override
    public String createOrUpdateIncidentTicket(IncidentPayload payload, RcaResult rcaResult) {
        log.info("jira.connector.disabled correlationId={}", payload.signal().correlationId());
        return "DISABLED";
    }

    @Override
    public Map<String, Object> fetchTicketByKey(String ticketKey) {
        log.info("jira.connector.disabled ticketKey={}", ticketKey);
        return Map.of(
                "key", ticketKey,
                "status", "DISABLED",
                "message", "Jira connector is disabled."
        );
    }
}
