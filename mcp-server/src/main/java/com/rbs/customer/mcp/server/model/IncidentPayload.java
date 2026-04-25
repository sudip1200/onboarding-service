package com.rbs.customer.mcp.server.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record IncidentPayload(
        @NotBlank String timestamp,
        @NotBlank String criticality,
        @NotNull @Valid IncidentSignal signal,
        @NotNull Map<String, Object> enrichment
) {
}
