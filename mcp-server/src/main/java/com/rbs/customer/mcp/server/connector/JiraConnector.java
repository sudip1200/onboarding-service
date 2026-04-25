package com.rbs.customer.mcp.server.connector;

import com.rbs.customer.mcp.server.model.IncidentPayload;
import com.rbs.customer.mcp.server.model.RcaResult;

public interface JiraConnector {
    String createOrUpdateIncidentTicket(IncidentPayload payload, RcaResult rcaResult);
}
