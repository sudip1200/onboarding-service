package com.rbs.customer.mcp.server.service;

import com.rbs.customer.mcp.server.model.LlmQueryRequest;
import com.rbs.customer.mcp.server.model.LlmQueryResponse;

public interface LlmAssistantService {
    LlmQueryResponse ask(LlmQueryRequest request);
}
