package com.rbs.customer.mcp.server.api;

import com.rbs.customer.mcp.server.model.IncidentAnalysisResponse;
import com.rbs.customer.mcp.server.model.IncidentPayload;
import com.rbs.customer.mcp.server.service.IncidentOrchestrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final IncidentOrchestrationService incidentOrchestrationService;

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public IncidentAnalysisResponse processIncident(@Valid @RequestBody IncidentPayload payload) {
        return incidentOrchestrationService.processIncident(payload);
    }
}
