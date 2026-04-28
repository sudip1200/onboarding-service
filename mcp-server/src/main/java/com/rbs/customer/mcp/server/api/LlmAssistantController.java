package com.rbs.customer.mcp.server.api;

import com.rbs.customer.mcp.server.model.LlmQueryRequest;
import com.rbs.customer.mcp.server.model.LlmQueryResponse;
import com.rbs.customer.mcp.server.service.LlmAssistantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/assistant")
@RequiredArgsConstructor
public class LlmAssistantController {

    private final LlmAssistantService llmAssistantService;

    @PostMapping("/query")
    @ResponseStatus(HttpStatus.OK)
    public LlmQueryResponse ask(@Valid @RequestBody LlmQueryRequest request) {
        return llmAssistantService.ask(request);
    }
}
