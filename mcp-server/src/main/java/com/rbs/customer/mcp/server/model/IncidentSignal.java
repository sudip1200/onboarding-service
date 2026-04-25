package com.rbs.customer.mcp.server.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record IncidentSignal(
        @NotBlank String correlationId,
        @NotBlank String endpoint,
        @NotBlank String errorCode,
        @Positive int statusCode,
        @NotBlank String failureType,
        String message,
        @NotBlank String eventTimestamp
) {
}
