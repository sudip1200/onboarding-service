package com.rbs.customer.mcp.server.connector;

import com.rbs.customer.mcp.server.model.IncidentPayload;
import com.rbs.customer.mcp.server.model.RcaResult;

import java.util.Map;

public interface JiraConnector {
    String createOrUpdateIncidentTicket(IncidentPayload payload, RcaResult rcaResult);
    Map<String, Object> fetchTicketByKey(String ticketKey);
}
